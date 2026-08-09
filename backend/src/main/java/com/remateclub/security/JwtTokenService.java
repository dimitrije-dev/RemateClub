package com.remateclub.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;

  public JwtTokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
    this.jwtEncoder = jwtEncoder;
    this.jwtProperties = jwtProperties;
  }

  public JwtAccessToken createAccessToken(
    UUID userId,
    String email,
    Collection<String> roles
  ) {
    Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    Instant expiresAt = issuedAt.plus(jwtProperties.accessTokenTtl());
    List<String> normalizedRoles = roles == null ? List.of() : roles.stream()
      .filter(role -> role != null && !role.isBlank())
      .map(String::trim)
      .distinct()
      .toList();

    JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
      .issuer(jwtProperties.issuer())
      .issuedAt(issuedAt)
      .expiresAt(expiresAt)
      .subject(userId.toString())
      .claim("roles", normalizedRoles);

    if (email != null && !email.isBlank()) {
      claims.claim("email", email);
    }

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    String token = jwtEncoder
      .encode(JwtEncoderParameters.from(header, claims.build()))
      .getTokenValue();

    return new JwtAccessToken(token, expiresAt);
  }
}
