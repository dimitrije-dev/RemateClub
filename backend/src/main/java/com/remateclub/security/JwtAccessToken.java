package com.remateclub.security;

import java.time.Instant;

public record JwtAccessToken(String token, Instant expiresAt) {
}
