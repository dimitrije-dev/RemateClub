package com.remateclub.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.remateclub.user.UserRole;
import com.remateclub.user.UserStatus;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock
  private AuthService authService;

  @Test
  void currentUserEndpointUsesJwtSubjectAsUserId() {
    UUID userId = UUID.randomUUID();
    UserResponse expectedResponse = new UserResponse(
      userId,
      "player@example.com",
      "Nikola",
      "Jokic",
      UserRole.PLAYER,
      UserStatus.ACTIVE
    );
    when(authService.currentUser(userId)).thenReturn(expectedResponse);
    AuthController controller = new AuthController(authService);

    UserResponse response = controller.me(jwtWithSubject(userId.toString()));

    assertThat(response).isEqualTo(expectedResponse);
    ArgumentCaptor<UUID> userIdCaptor = ArgumentCaptor.forClass(UUID.class);
    verify(authService).currentUser(userIdCaptor.capture());
    assertThat(userIdCaptor.getValue()).isEqualTo(userId);
  }

  @Test
  void loginEndpointDelegatesToAuthService() {
    AuthController controller = new AuthController(authService);
    LoginRequest request = new LoginRequest("player@example.com", "strongPassword123");
    AuthResponse expectedResponse = authResponse(UUID.randomUUID());
    when(authService.login(request)).thenReturn(expectedResponse);

    AuthResponse response = controller.login(request);

    assertThat(response).isEqualTo(expectedResponse);
    verify(authService).login(request);
  }

  @Test
  void registerEndpointDelegatesToAuthService() {
    AuthController controller = new AuthController(authService);
    RegisterRequest request = new RegisterRequest(
      "player@example.com",
      "strongPassword123",
      "Nikola",
      "Jokic",
      UserRole.PLAYER
    );
    AuthResponse expectedResponse = authResponse(UUID.randomUUID());
    when(authService.register(request)).thenReturn(expectedResponse);

    AuthResponse response = controller.register(request);

    assertThat(response).isEqualTo(expectedResponse);
    verify(authService).register(request);
  }

  @Test
  void refreshEndpointDelegatesToAuthService() {
    AuthController controller = new AuthController(authService);
    RefreshRequest request = new RefreshRequest("refresh-token");
    AuthResponse expectedResponse = authResponse(UUID.randomUUID());
    when(authService.refresh(request)).thenReturn(expectedResponse);

    AuthResponse response = controller.refresh(request);

    assertThat(response).isEqualTo(expectedResponse);
    verify(authService).refresh(request);
  }

  @Test
  void currentUserEndpointRejectsInvalidJwtSubject() {
    AuthController controller = new AuthController(authService);

    assertThatThrownBy(() -> controller.me(jwtWithSubject("not-a-uuid")))
      .isInstanceOf(BadCredentialsException.class)
      .hasMessage("Invalid token subject");
  }

  private Jwt jwtWithSubject(String subject) {
    return new Jwt(
      "token",
      Instant.now(),
      Instant.now().plusSeconds(900),
      Map.of("alg", "HS256"),
      Map.of("sub", subject)
    );
  }

  private AuthResponse authResponse(UUID userId) {
    return new AuthResponse(
      "access-token",
      "Bearer",
      Instant.now().plusSeconds(900),
      "refresh-token",
      Instant.now().plusSeconds(2_592_000),
      new UserResponse(
        userId,
        "player@example.com",
        "Nikola",
        "Jokic",
        UserRole.PLAYER,
        UserStatus.ACTIVE
      )
    );
  }
}
