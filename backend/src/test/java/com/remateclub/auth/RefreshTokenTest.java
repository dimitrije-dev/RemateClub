package com.remateclub.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  @Test
  void newRefreshTokenIsActiveBeforeExpiration() {
    Instant now = Instant.parse("2026-08-09T12:00:00Z");
    RefreshToken refreshToken = new RefreshToken(
      UUID.randomUUID(),
      "hashed-token",
      now.plusSeconds(60)
    );

    assertThat(refreshToken.isActive(now)).isTrue();
    assertThat(refreshToken.isRevoked()).isFalse();
    assertThat(refreshToken.isExpired(now)).isFalse();
  }

  @Test
  void expiredRefreshTokenIsNotActive() {
    Instant now = Instant.parse("2026-08-09T12:00:00Z");
    RefreshToken refreshToken = new RefreshToken(
      UUID.randomUUID(),
      "hashed-token",
      now
    );

    assertThat(refreshToken.isExpired(now)).isTrue();
    assertThat(refreshToken.isActive(now)).isFalse();
  }

  @Test
  void revokedRefreshTokenIsNotActive() {
    Instant now = Instant.parse("2026-08-09T12:00:00Z");
    RefreshToken refreshToken = new RefreshToken(
      UUID.randomUUID(),
      "hashed-token",
      now.plusSeconds(60)
    );

    refreshToken.revoke(now);

    assertThat(refreshToken.isRevoked()).isTrue();
    assertThat(refreshToken.getRevokedAt()).isEqualTo(now);
    assertThat(refreshToken.isActive(now)).isFalse();
  }

  @Test
  void replacedRefreshTokenTracksReplacement() {
    Instant now = Instant.parse("2026-08-09T12:00:00Z");
    UUID replacementTokenId = UUID.randomUUID();
    RefreshToken refreshToken = new RefreshToken(
      UUID.randomUUID(),
      "hashed-token",
      now.plusSeconds(60)
    );

    refreshToken.replaceWith(replacementTokenId, now);

    assertThat(refreshToken.isRevoked()).isTrue();
    assertThat(refreshToken.getRevokedAt()).isEqualTo(now);
    assertThat(refreshToken.getReplacedByTokenId()).isEqualTo(replacementTokenId);
  }
}
