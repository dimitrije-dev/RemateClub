package com.remateclub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.remateclub.common.exception.BadRequestException;
import com.remateclub.common.exception.ConflictException;
import com.remateclub.common.exception.ResourceNotFoundException;
import com.remateclub.security.JwtProperties;
import com.remateclub.security.JwtTokenService;
import com.remateclub.user.User;
import com.remateclub.user.UserRepository;
import com.remateclub.user.UserRole;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final String SECRET = "test-secret-with-at-least-32-bytes";

  @Mock
  private UserRepository userRepository;

  @Mock
  private RefreshTokenRepository refreshTokenRepository;

  private PasswordEncoder passwordEncoder;
  private JwtProperties jwtProperties;
  private RefreshTokenService refreshTokenService;
  private AuthService authService;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    jwtProperties = new JwtProperties("remate-club-test", SECRET, Duration.ofMinutes(15));
    refreshTokenService = new RefreshTokenService(
      refreshTokenRepository,
      new RefreshTokenProperties(Duration.ofDays(30))
    );
    authService = new AuthService(
      userRepository,
      passwordEncoder,
      new JwtTokenService(jwtEncoder(jwtProperties), jwtProperties),
      refreshTokenService
    );
  }

  @Test
  void registersUserWithBcryptPasswordHashAndAccessToken() {
    when(userRepository.existsByEmail("player@example.com")).thenReturn(false);
    when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      setId(user, UUID.randomUUID());
      return user;
    });
    stubRefreshTokenSave();

    AuthResponse response = authService.register(new RegisterRequest(
      " PLAYER@Example.com ",
      "strongPassword123",
      " Nikola ",
      " Jokic ",
      UserRole.PLAYER
    ));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).saveAndFlush(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertThat(savedUser.getEmail()).isEqualTo("player@example.com");
    assertThat(savedUser.getFirstName()).isEqualTo("Nikola");
    assertThat(savedUser.getLastName()).isEqualTo("Jokic");
    assertThat(savedUser.getRole()).isEqualTo(UserRole.PLAYER);
    assertThat(savedUser.getPasswordHash()).isNotEqualTo("strongPassword123");
    assertThat(passwordEncoder.matches("strongPassword123", savedUser.getPasswordHash())).isTrue();
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.refreshToken()).isNotBlank();
    assertThat(response.refreshTokenExpiresAt()).isAfter(response.expiresAt());

    Jwt decoded = jwtDecoder(jwtProperties).decode(response.accessToken());
    assertThat(decoded.getSubject()).isEqualTo(response.user().id().toString());
    assertThat(decoded.getClaimAsString("email")).isEqualTo("player@example.com");
    assertThat(decoded.getClaimAsStringList("roles")).containsExactly("PLAYER");
  }

  @Test
  void rejectsDuplicateEmailDuringRegistration() {
    when(userRepository.existsByEmail("player@example.com")).thenReturn(true);

    assertThatThrownBy(() -> authService.register(new RegisterRequest(
        "player@example.com",
        "strongPassword123",
        "Nikola",
        "Jokic",
        UserRole.PLAYER
      )))
      .isInstanceOf(ConflictException.class)
      .hasMessage("Email already exists");
  }

  @Test
  void mapsDatabaseEmailRaceToConflict() {
    when(userRepository.existsByEmail("player@example.com")).thenReturn(false);
    when(userRepository.saveAndFlush(any(User.class)))
      .thenThrow(new DataIntegrityViolationException("unique email"));

    assertThatThrownBy(() -> authService.register(new RegisterRequest(
        "player@example.com",
        "strongPassword123",
        "Nikola",
        "Jokic",
        UserRole.PLAYER
      )))
      .isInstanceOf(ConflictException.class)
      .hasMessage("Email already exists");
  }

  @Test
  void rejectsPublicAdminRegistration() {
    assertThatThrownBy(() -> authService.register(new RegisterRequest(
        "admin@example.com",
        "strongPassword123",
        "Admin",
        "User",
        UserRole.ADMIN
      )))
      .isInstanceOf(BadRequestException.class)
      .hasMessage("Admin accounts cannot be created through public registration");
  }

  @Test
  void logsInWithMatchingPasswordAndReturnsAccessToken() {
    User user = new User(
      "owner@example.com",
      passwordEncoder.encode("strongPassword123"),
      "Ana",
      "Ivanovic",
      UserRole.OWNER
    );
    UUID userId = UUID.randomUUID();
    setId(user, userId);

    when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
    stubRefreshTokenSave();

    AuthResponse response = authService.login(new LoginRequest(
      "OWNER@example.com",
      "strongPassword123"
    ));

    assertThat(response.user().id()).isEqualTo(userId);
    assertThat(response.user().role()).isEqualTo(UserRole.OWNER);
    assertThat(response.refreshToken()).isNotBlank();

    Jwt decoded = jwtDecoder(jwtProperties).decode(response.accessToken());
    assertThat(decoded.getSubject()).isEqualTo(userId.toString());
    assertThat(decoded.getClaimAsStringList("roles")).containsExactly("OWNER");
  }

  @Test
  void rejectsLoginWithWrongPassword() {
    User user = new User(
      "player@example.com",
      passwordEncoder.encode("strongPassword123"),
      "Nikola",
      "Jokic",
      UserRole.PLAYER
    );

    when(userRepository.findByEmail("player@example.com")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> authService.login(new LoginRequest(
        "player@example.com",
        "wrong-password"
      )))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("Invalid email or password");
  }

  @Test
  void refreshesAccessTokenAndRotatesRefreshToken() {
    UUID userId = UUID.randomUUID();
    String rawRefreshToken = "existing-refresh-token";
    RefreshToken currentRefreshToken = new RefreshToken(
      userId,
      refreshTokenService.hashToken(rawRefreshToken),
      java.time.Instant.now().plus(Duration.ofDays(1))
    );
    setId(currentRefreshToken, UUID.randomUUID());
    User user = new User(
      "player@example.com",
      passwordEncoder.encode("strongPassword123"),
      "Nikola",
      "Jokic",
      UserRole.PLAYER
    );
    setId(user, userId);

    when(refreshTokenRepository.findByTokenHashForUpdate(refreshTokenService.hashToken(rawRefreshToken)))
      .thenReturn(Optional.of(currentRefreshToken));
    stubRefreshTokenSave();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    AuthResponse response = authService.refresh(new RefreshRequest(rawRefreshToken));

    assertThat(currentRefreshToken.isRevoked()).isTrue();
    assertThat(currentRefreshToken.getReplacedByTokenId()).isNotNull();
    assertThat(response.accessToken()).isNotBlank();
    assertThat(response.refreshToken()).isNotBlank();
    assertThat(response.refreshToken()).isNotEqualTo(rawRefreshToken);
    assertThat(response.user().id()).isEqualTo(userId);

    Jwt decoded = jwtDecoder(jwtProperties).decode(response.accessToken());
    assertThat(decoded.getSubject()).isEqualTo(userId.toString());
  }

  @Test
  void rejectsInvalidRefreshToken() {
    String rawRefreshToken = "missing-refresh-token";
    when(refreshTokenRepository.findByTokenHashForUpdate(refreshTokenService.hashToken(rawRefreshToken)))
      .thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refresh(new RefreshRequest(rawRefreshToken)))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("Invalid refresh token");
  }

  @Test
  void returnsCurrentUserByAuthenticatedUserId() {
    UUID userId = UUID.randomUUID();
    User user = new User(
      "player@example.com",
      passwordEncoder.encode("strongPassword123"),
      "Nikola",
      "Jokic",
      UserRole.PLAYER
    );
    setId(user, userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    UserResponse response = authService.currentUser(userId);

    assertThat(response.id()).isEqualTo(userId);
    assertThat(response.email()).isEqualTo("player@example.com");
    assertThat(response.firstName()).isEqualTo("Nikola");
    assertThat(response.lastName()).isEqualTo("Jokic");
    assertThat(response.role()).isEqualTo(UserRole.PLAYER);
  }

  @Test
  void rejectsCurrentUserLookupWhenUserDoesNotExist() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.currentUser(userId))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("User not found");
  }

  private JwtEncoder jwtEncoder(JwtProperties properties) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(signingKey(properties)));
  }

  private JwtDecoder jwtDecoder(JwtProperties properties) {
    return NimbusJwtDecoder
      .withSecretKey(signingKey(properties))
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
  }

  private SecretKey signingKey(JwtProperties properties) {
    return new SecretKeySpec(
      properties.secret().getBytes(StandardCharsets.UTF_8),
      "HmacSHA256"
    );
  }

  private void setId(User user, UUID id) {
    try {
      Field idField = User.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(user, id);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Unable to assign test user id", exception);
    }
  }

  private void setId(RefreshToken refreshToken, UUID id) {
    try {
      Field idField = RefreshToken.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(refreshToken, id);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Unable to assign test refresh token id", exception);
    }
  }

  private void stubRefreshTokenSave() {
    when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
      RefreshToken refreshToken = invocation.getArgument(0);
      setId(refreshToken, UUID.randomUUID());
      return refreshToken;
    });
  }
}
