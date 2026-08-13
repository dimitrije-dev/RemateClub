package com.remateclub.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

  private static final int TOKEN_BYTES = 32;
  private static final int MAX_GENERATION_ATTEMPTS = 5;

  private final RefreshTokenRepository refreshTokenRepository;
  private final RefreshTokenProperties refreshTokenProperties;
  private final SecureRandom secureRandom;

  public RefreshTokenService(
    RefreshTokenRepository refreshTokenRepository,
    RefreshTokenProperties refreshTokenProperties
  ) {
    this(refreshTokenRepository, refreshTokenProperties, new SecureRandom());
  }

  RefreshTokenService(
    RefreshTokenRepository refreshTokenRepository,
    RefreshTokenProperties refreshTokenProperties,
    SecureRandom secureRandom
  ) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenProperties = refreshTokenProperties;
    this.secureRandom = secureRandom;
  }

  @Transactional
  public RefreshTokenSession createForUser(UUID userId) {
    for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
      String token = randomToken();
      String tokenHash = hashToken(token);

      if (!refreshTokenRepository.existsByTokenHash(tokenHash)) {
        Instant expiresAt = Instant.now().plus(refreshTokenProperties.ttl());
        RefreshToken savedToken = refreshTokenRepository.save(new RefreshToken(userId, tokenHash, expiresAt));
        return new RefreshTokenSession(savedToken.getId(), userId, token, expiresAt);
      }
    }

    throw new IllegalStateException("Unable to create unique refresh token");
  }

  @Transactional
  public RefreshTokenSession rotate(String rawToken) {
    Instant now = Instant.now();
    RefreshToken currentToken = requireActiveToken(rawToken, now);

    RefreshTokenSession replacementSession = createForUser(currentToken.getUserId());
    currentToken.replaceWith(replacementSession.id(), now);

    return replacementSession;
  }

  @Transactional(readOnly = true)
  public UUID userIdForActiveToken(String rawToken) {
    return requireActiveToken(rawToken, Instant.now()).getUserId();
  }

  String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }

  private String randomToken() {
    byte[] bytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private RefreshToken requireActiveToken(String rawToken, Instant now) {
    return refreshTokenRepository.findByTokenHash(hashToken(rawToken))
      .filter(refreshToken -> refreshToken.isActive(now))
      .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
  }
}
