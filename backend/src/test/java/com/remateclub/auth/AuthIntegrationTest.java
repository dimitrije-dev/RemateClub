package com.remateclub.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.remateclub.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(AuthIntegrationTest.AdminOnlyTestController.class)
class AuthIntegrationTest {

  private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
    new ParameterizedTypeReference<>() { };

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
    DockerImageName.parse("postgres:16-alpine")
  );

  @DynamicPropertySource
  static void databaseProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("remate-club.security.jwt.secret", () -> "integration-secret-with-at-least-32-bytes");
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void cleanDatabase() {
    refreshTokenRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void flywayAppliesUsersMigrationAndHibernateValidationStarts() {
    String latestVersion = jdbcTemplate.queryForObject(
      "select version from flyway_schema_history where success = true order by installed_rank desc limit 1",
      String.class
    );
    Integer usersTableCount = jdbcTemplate.queryForObject(
      "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'users'",
      Integer.class
    );

    assertThat(latestVersion).isEqualTo("3");
    assertThat(usersTableCount).isEqualTo(1);
  }

  @Test
  void registerLoginRefreshAndCurrentUserWorkAgainstPostgreSql() {
    ResponseEntity<Map<String, Object>> registration = register("player@example.com");
    assertThat(registration.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(registration.getBody()).isNotNull();

    String accessToken = registration.getBody().get("accessToken").toString();
    String refreshToken = registration.getBody().get("refreshToken").toString();

    ResponseEntity<Map<String, Object>> currentUser = getWithBearer("/api/auth/me", accessToken);
    assertThat(currentUser.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(currentUser.getBody()).containsEntry("email", "player@example.com");

    ResponseEntity<Map<String, Object>> login = restTemplate.exchange(
      "/api/auth/login",
      HttpMethod.POST,
      jsonEntity(Map.of("email", "PLAYER@example.com", "password", "strongPassword123")),
      JSON_OBJECT
    );
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<Map<String, Object>> refresh = refresh(refreshToken);
    assertThat(refresh.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(refresh.getBody()).isNotNull();
    assertThat(refresh.getBody().get("refreshToken")).isNotEqualTo(refreshToken);

    ResponseEntity<Map<String, Object>> reusedToken = refresh(refreshToken);
    assertThat(reusedToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(reusedToken.getBody()).containsEntry("code", "UNAUTHORIZED");
  }

  @Test
  void duplicateEmailReturnsConflictEvenWithDatabaseConstraint() {
    assertThat(register("duplicate@example.com").getStatusCode()).isEqualTo(HttpStatus.CREATED);

    ResponseEntity<Map<String, Object>> duplicate = register("duplicate@example.com");

    assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(duplicate.getBody()).containsEntry("code", "CONFLICT");
  }

  @Test
  void onlyOneConcurrentRefreshCanRotateTheSameToken() throws Exception {
    String refreshToken = register("concurrent@example.com")
      .getBody()
      .get("refreshToken")
      .toString();
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);

    try {
      List<Future<Integer>> futures = new ArrayList<>();
      for (int index = 0; index < 2; index++) {
        futures.add(executor.submit(() -> {
          start.await();
          return refresh(refreshToken).getStatusCode().value();
        }));
      }

      start.countDown();
      List<Integer> statuses = new ArrayList<>();
      for (Future<Integer> future : futures) {
        statuses.add(future.get());
      }

      assertThat(statuses).containsExactlyInAnyOrder(200, 401);
      assertThat(refreshTokenRepository.findAll()).hasSize(2);
    } finally {
      executor.shutdownNow();
    }
  }

  @Test
  void securityErrorsAndCorsPreflightUseFrontendFriendlyResponses() {
    ResponseEntity<Map<String, Object>> unauthenticated = restTemplate.exchange(
      "/api/auth/me",
      HttpMethod.GET,
      HttpEntity.EMPTY,
      JSON_OBJECT
    );
    assertThat(unauthenticated.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(unauthenticated.getBody()).containsEntry("code", "UNAUTHORIZED");

    String playerToken = register("player@example.com").getBody().get("accessToken").toString();
    ResponseEntity<Map<String, Object>> forbidden = getWithBearer("/api/test/admin", playerToken);
    assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(forbidden.getBody()).containsEntry("code", "FORBIDDEN");

    HttpHeaders preflightHeaders = new HttpHeaders();
    preflightHeaders.setOrigin("http://localhost:5174");
    preflightHeaders.set("Access-Control-Request-Method", "POST");
    ResponseEntity<Void> preflight = restTemplate.exchange(
      "/api/auth/login",
      HttpMethod.OPTIONS,
      new HttpEntity<>(preflightHeaders),
      Void.class
    );
    assertThat(preflight.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(preflight.getHeaders().getAccessControlAllowOrigin()).isEqualTo("http://localhost:5174");
  }

  private ResponseEntity<Map<String, Object>> register(String email) {
    return restTemplate.exchange(
      "/api/auth/register",
      HttpMethod.POST,
      jsonEntity(Map.of(
        "email", email,
        "password", "strongPassword123",
        "firstName", "Nikola",
        "lastName", "Jokic",
        "role", "PLAYER"
      )),
      JSON_OBJECT
    );
  }

  private ResponseEntity<Map<String, Object>> refresh(String refreshToken) {
    return restTemplate.exchange(
      "/api/auth/refresh",
      HttpMethod.POST,
      jsonEntity(Map.of("refreshToken", refreshToken)),
      JSON_OBJECT
    );
  }

  private ResponseEntity<Map<String, Object>> getWithBearer(String path, String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), JSON_OBJECT);
  }

  private HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
    return new HttpEntity<>(body, headers);
  }

  @RestController
  static class AdminOnlyTestController {

    @GetMapping("/api/test/admin")
    @PreAuthorize("hasRole('ADMIN')")
    Map<String, String> adminOnly() {
      return Map.of("status", "ok");
    }
  }
}
