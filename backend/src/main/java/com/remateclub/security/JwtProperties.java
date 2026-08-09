package com.remateclub.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "remate-club.security.jwt")
public record JwtProperties(
  String issuer,
  String secret,
  Duration accessTokenTtl
) {

  private static final Duration DEFAULT_ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
  private static final int MIN_HMAC_SHA_256_KEY_BYTES = 32;

  public JwtProperties {
    if (issuer == null || issuer.isBlank()) {
      issuer = "remate-club";
    }

    if (secret == null || secret.isBlank()) {
      throw new IllegalStateException("JWT secret must be configured");
    }

    if (accessTokenTtl == null) {
      accessTokenTtl = DEFAULT_ACCESS_TOKEN_TTL;
    }
  }

  SecretKey signingKey() {
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

    if (secretBytes.length < MIN_HMAC_SHA_256_KEY_BYTES) {
      throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256");
    }

    return new SecretKeySpec(secretBytes, "HmacSHA256");
  }
}
