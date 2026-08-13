package com.remateclub.auth;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "remate-club.security.refresh-token")
public record RefreshTokenProperties(Duration ttl) {

  private static final Duration DEFAULT_TTL = Duration.ofDays(30);

  public RefreshTokenProperties {
    if (ttl == null) {
      ttl = DEFAULT_TTL;
    }
  }
}
