package com.remateclub.auth;

import com.remateclub.common.exception.BadRequestException;
import com.remateclub.common.exception.ConflictException;
import com.remateclub.common.exception.ResourceNotFoundException;
import com.remateclub.security.JwtAccessToken;
import com.remateclub.security.JwtTokenService;
import com.remateclub.user.User;
import com.remateclub.user.UserRepository;
import com.remateclub.user.UserRole;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;
  private final RefreshTokenService refreshTokenService;

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    JwtTokenService jwtTokenService,
    RefreshTokenService refreshTokenService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.refreshTokenService = refreshTokenService;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (request.role() == UserRole.ADMIN) {
      throw new BadRequestException("Admin accounts cannot be created through public registration");
    }

    String email = normalizeEmail(request.email());

    if (userRepository.existsByEmail(email)) {
      throw new ConflictException("Email already exists");
    }

    User user = new User(
      email,
      passwordEncoder.encode(request.password()),
      request.firstName().trim(),
      request.lastName().trim(),
      request.role()
    );

    User savedUser;
    try {
      savedUser = userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException exception) {
      throw new ConflictException("Email already exists");
    }

    return authResponse(savedUser);
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());
    User user = userRepository.findByEmail(email)
      .filter(foundUser -> passwordEncoder.matches(request.password(), foundUser.getPasswordHash()))
      .filter(User::canAuthenticate)
      .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

    return authResponse(user);
  }

  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    RefreshTokenSession refreshToken = refreshTokenService.rotate(request.refreshToken());
    User user = userRepository.findById(refreshToken.userId())
      .filter(User::canAuthenticate)
      .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    return authResponse(user, refreshToken);
  }

  @Transactional(readOnly = true)
  public UserResponse currentUser(UUID userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    return UserResponse.from(user);
  }

  private AuthResponse authResponse(User user) {
    return authResponse(user, refreshTokenService.createForUser(user.getId()));
  }

  private AuthResponse authResponse(User user, RefreshTokenSession refreshToken) {
    JwtAccessToken accessToken = jwtTokenService.createAccessToken(
      user.getId(),
      user.getEmail(),
      List.of(user.getRole().name())
    );

    return new AuthResponse(
      accessToken.token(),
      "Bearer",
      accessToken.expiresAt(),
      refreshToken.token(),
      refreshToken.expiresAt(),
      UserResponse.from(user)
    );
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
