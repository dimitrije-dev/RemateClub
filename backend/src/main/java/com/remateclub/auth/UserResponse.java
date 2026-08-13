package com.remateclub.auth;

import com.remateclub.user.User;
import com.remateclub.user.UserRole;
import com.remateclub.user.UserStatus;
import java.util.UUID;

public record UserResponse(
  UUID id,
  String email,
  String firstName,
  String lastName,
  UserRole role,
  UserStatus status
) {

  static UserResponse from(User user) {
    return new UserResponse(
      user.getId(),
      user.getEmail(),
      user.getFirstName(),
      user.getLastName(),
      user.getRole(),
      user.getStatus()
    );
  }
}
