package com.remateclub.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtTokenServiceTest {

  private static final String SECRET = "test-secret-with-at-least-32-bytes";

  @Test
  void createsSignedAccessTokenWithExpectedClaims() {
    JwtProperties properties = new JwtProperties(
      "remate-club-test",
      SECRET,
      Duration.ofMinutes(15)
    );
    JwtTokenService tokenService = new JwtTokenService(jwtEncoder(properties), properties);
    UUID userId = UUID.randomUUID();
    Instant beforeCreate = Instant.now().truncatedTo(ChronoUnit.SECONDS);

    JwtAccessToken accessToken = tokenService.createAccessToken(
      userId,
      "player@example.com",
      List.of("PLAYER", "OWNER", "PLAYER")
    );

    Jwt decoded = jwtDecoder(properties).decode(accessToken.token());

    assertThat(decoded.getClaimAsString("iss")).isEqualTo("remate-club-test");
    assertThat(decoded.getSubject()).isEqualTo(userId.toString());
    assertThat(decoded.getClaimAsString("email")).isEqualTo("player@example.com");
    assertThat(decoded.getClaimAsStringList("roles")).containsExactly("PLAYER", "OWNER");
    assertThat(decoded.getIssuedAt()).isAfterOrEqualTo(beforeCreate);
    assertThat(decoded.getExpiresAt()).isEqualTo(accessToken.expiresAt());
    assertThat(accessToken.expiresAt()).isAfter(beforeCreate.plus(Duration.ofMinutes(14)));
  }

  @Test
  void omitsBlankEmailAndBlankRoles() {
    JwtProperties properties = new JwtProperties(
      "remate-club-test",
      SECRET,
      Duration.ofMinutes(15)
    );
    JwtTokenService tokenService = new JwtTokenService(jwtEncoder(properties), properties);

    JwtAccessToken accessToken = tokenService.createAccessToken(
      UUID.randomUUID(),
      " ",
      List.of("PLAYER", " ", "")
    );

    Jwt decoded = jwtDecoder(properties).decode(accessToken.token());

    assertThat(decoded.hasClaim("email")).isFalse();
    assertThat(decoded.getClaimAsStringList("roles")).containsExactly("PLAYER");
  }

  @Test
  void rejectsShortJwtSecret() {
    JwtProperties properties = new JwtProperties(
      "remate-club-test",
      "short-secret",
      Duration.ofMinutes(15)
    );

    assertThatThrownBy(properties::signingKey)
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("JWT secret must be at least 32 bytes for HS256");
  }

  private JwtEncoder jwtEncoder(JwtProperties properties) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(properties.signingKey()));
  }

  private JwtDecoder jwtDecoder(JwtProperties properties) {
    return NimbusJwtDecoder
      .withSecretKey(properties.signingKey())
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
  }
}
