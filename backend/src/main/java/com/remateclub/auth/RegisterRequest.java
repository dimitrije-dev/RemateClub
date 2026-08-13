package com.remateclub.auth;

import com.remateclub.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
  @NotBlank @Email @Size(max = 320) String email,
  @NotBlank @Size(min = 8, max = 72) String password,
  @NotBlank @Size(max = 80) String firstName,
  @NotBlank @Size(max = 80) String lastName,
  @NotNull UserRole role
) {
}
