package com.remateclub.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
  @NotBlank String refreshToken
) {
}
