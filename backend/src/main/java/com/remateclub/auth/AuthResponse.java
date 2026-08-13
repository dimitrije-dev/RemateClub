package com.remateclub.auth;

import java.time.Instant;

public record AuthResponse(
  String accessToken,
  String tokenType,
  Instant expiresAt,
  String refreshToken,
  Instant refreshTokenExpiresAt,
  UserResponse user
) {
}
