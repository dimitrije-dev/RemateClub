package com.remateclub.auth;

import java.time.Instant;
import java.util.UUID;

record RefreshTokenSession(
  UUID id,
  UUID userId,
  String token,
  Instant expiresAt
) {
}
