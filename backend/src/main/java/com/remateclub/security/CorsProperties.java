package com.remateclub.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "remate-club.security.cors")
public record CorsProperties(List<String> allowedOrigins) {

  private static final List<String> DEFAULT_ALLOWED_ORIGINS = List.of(
    "http://localhost:5173",
    "http://localhost:5174"
  );

  public CorsProperties {
    if (allowedOrigins == null || allowedOrigins.isEmpty()) {
      allowedOrigins = DEFAULT_ALLOWED_ORIGINS;
    }
  }
}
