package com.remateclub.club;

import static org.assertj.core.api.Assertions.assertThat;

import com.remateclub.auth.RefreshTokenRepository;
import com.remateclub.court.Court;
import com.remateclub.court.CourtRepository;
import com.remateclub.court.CourtType;
import com.remateclub.security.JwtTokenService;
import com.remateclub.user.User;
import com.remateclub.user.UserRepository;
import com.remateclub.user.UserRole;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClubOwnershipIntegrationTest {

  private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
    new ParameterizedTypeReference<>() { };
  private static final ParameterizedTypeReference<List<Map<String, Object>>> JSON_LIST =
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
    registry.add("remate-club.security.jwt.secret", () -> "ownership-test-secret-with-at-least-32-bytes");
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ClubRepository clubRepository;

  @Autowired
  private CourtRepository courtRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtTokenService jwtTokenService;

  @BeforeEach
  void cleanDatabase() {
    refreshTokenRepository.deleteAll();
    courtRepository.deleteAll();
    clubRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void ownerCannotUpdateAnotherOwnersClub() {
    String firstOwnerToken = register("first-owner@example.com", "OWNER");
    String secondOwnerToken = register("second-owner@example.com", "OWNER");
    UUID clubId = createClub(firstOwnerToken, "Prvi klub", "Beograd");

    ResponseEntity<Map<String, Object>> forbidden = exchangeWithBearer(
      "/api/owner/clubs/" + clubId,
      HttpMethod.PUT,
      secondOwnerToken,
      Map.of("name", "Preuzet klub", "city", "Novi Sad")
    );

    assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(forbidden.getBody()).containsEntry("code", "FORBIDDEN");
    Club unchanged = clubRepository.findById(clubId).orElseThrow();
    assertThat(unchanged.getName()).isEqualTo("Prvi klub");
    assertThat(unchanged.getCity()).isEqualTo("Beograd");
  }

  @Test
  void ownerCanUpdateOwnClub() {
    String ownerToken = register("owner@example.com", "OWNER");
    UUID clubId = createClub(ownerToken, "Stari naziv", "Beograd");

    ResponseEntity<Map<String, Object>> updated = exchangeWithBearer(
      "/api/owner/clubs/" + clubId,
      HttpMethod.PUT,
      ownerToken,
      Map.of("name", "Novi naziv", "city", "Novi Sad")
    );

    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updated.getBody())
      .containsEntry("name", "Novi naziv")
      .containsEntry("city", "Novi Sad")
      .containsEntry("status", "PENDING_APPROVAL");
  }

  @Test
  void ownerListContainsOnlyAuthenticatedOwnersClubs() {
    String firstOwnerToken = register("list-first-owner@example.com", "OWNER");
    String secondOwnerToken = register("list-second-owner@example.com", "OWNER");
    UUID firstClubId = createClub(firstOwnerToken, "Prvi vlasnikov klub", "Beograd");
    createClub(secondOwnerToken, "Tudji klub", "Novi Sad");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(firstOwnerToken);
    ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
      "/api/owner/clubs",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      JSON_LIST
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).singleElement().satisfies(club -> {
      assertThat(club)
        .containsEntry("id", firstClubId.toString())
        .containsEntry("name", "Prvi vlasnikov klub")
        .containsEntry("city", "Beograd")
        .containsEntry("status", "PENDING_APPROVAL");
    });
  }

  @Test
  void playerCannotUseOwnerClubEndpoints() {
    String playerToken = register("player@example.com", "PLAYER");

    ResponseEntity<Map<String, Object>> forbidden = exchangeWithBearer(
      "/api/owner/clubs",
      HttpMethod.POST,
      playerToken,
      Map.of("name", "Nedozvoljeni klub", "city", "Beograd")
    );

    assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(forbidden.getBody()).containsEntry("code", "FORBIDDEN");
    assertThat(clubRepository.count()).isZero();
  }

  @Test
  void adminCanApproveAndRejectClub() {
    String ownerToken = register("approval-owner@example.com", "OWNER");
    String adminToken = createAdminToken("admin@example.com");
    UUID clubId = createClub(ownerToken, "Klub za proveru", "Beograd");

    ResponseEntity<Map<String, Object>> approved = exchangeWithBearer(
      "/api/admin/clubs/" + clubId + "/approve",
      HttpMethod.PATCH,
      adminToken,
      Map.of()
    );

    assertThat(approved.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(approved.getBody()).containsEntry("status", "APPROVED");
    assertThat(clubRepository.findById(clubId).orElseThrow().getStatus())
      .isEqualTo(ClubStatus.APPROVED);

    ResponseEntity<Map<String, Object>> rejected = exchangeWithBearer(
      "/api/admin/clubs/" + clubId + "/reject",
      HttpMethod.PATCH,
      adminToken,
      Map.of()
    );

    assertThat(rejected.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(rejected.getBody()).containsEntry("status", "REJECTED");
    assertThat(clubRepository.findById(clubId).orElseThrow().getStatus())
      .isEqualTo(ClubStatus.REJECTED);
  }

  @Test
  void adminPendingListContainsOnlyPendingClubs() {
    String ownerToken = register("pending-list-owner@example.com", "OWNER");
    String adminToken = createAdminToken("pending-list-admin@example.com");
    UUID pendingClubId = createClub(ownerToken, "Klub za pregled", "Beograd");
    UUID approvedClubId = createClub(ownerToken, "Vec odobren klub", "Novi Sad");
    approveClub(approvedClubId);

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(adminToken);
    ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
      "/api/admin/clubs/pending",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      JSON_LIST
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).singleElement().satisfies(club ->
      assertThat(club)
        .containsEntry("id", pendingClubId.toString())
        .containsEntry("status", "PENDING_APPROVAL")
    );
  }

  @Test
  void ownerCanCreateListAndUpdateCourtsOnlyForOwnClub() {
    String ownerToken = register("court-owner@example.com", "OWNER");
    String otherOwnerToken = register("court-other-owner@example.com", "OWNER");
    UUID clubId = createClub(ownerToken, "Klub sa terenima", "Beograd");

    ResponseEntity<Map<String, Object>> created = exchangeWithBearer(
      "/api/owner/clubs/" + clubId + "/courts",
      HttpMethod.POST,
      ownerToken,
      Map.of("name", "Teren A", "type", "PANORAMIC", "active", true, "hourlyPrice", 3200)
    );
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(created.getBody())
      .containsEntry("name", "Teren A")
      .containsEntry("type", "PANORAMIC")
      .containsEntry("active", true);
    UUID courtId = UUID.fromString(created.getBody().get("id").toString());

    HttpHeaders ownerHeaders = new HttpHeaders();
    ownerHeaders.setBearerAuth(ownerToken);
    ResponseEntity<List<Map<String, Object>>> listed = restTemplate.exchange(
      "/api/owner/clubs/" + clubId + "/courts",
      HttpMethod.GET,
      new HttpEntity<>(ownerHeaders),
      JSON_LIST
    );
    assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listed.getBody()).singleElement().satisfies(court ->
      assertThat(court).containsEntry("id", courtId.toString())
    );

    ResponseEntity<Map<String, Object>> updated = exchangeWithBearer(
      "/api/owner/clubs/" + clubId + "/courts/" + courtId,
      HttpMethod.PUT,
      ownerToken,
      Map.of("name", "Teren B", "type", "STANDARD", "active", false, "hourlyPrice", 2800)
    );
    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updated.getBody())
      .containsEntry("name", "Teren B")
      .containsEntry("type", "STANDARD")
      .containsEntry("active", false);

    ResponseEntity<Map<String, Object>> forbidden = exchangeWithBearer(
      "/api/owner/clubs/" + clubId + "/courts/" + courtId,
      HttpMethod.PUT,
      otherOwnerToken,
      Map.of("name", "Preuzet teren", "type", "SINGLE", "active", true, "hourlyPrice", 100)
    );
    assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(courtRepository.findById(courtId).orElseThrow().getName()).isEqualTo("Teren B");
  }

  @Test
  void ownerCannotApproveOrRejectClub() {
    String ownerToken = register("non-admin-owner@example.com", "OWNER");
    UUID clubId = createClub(ownerToken, "Klub bez admin odluke", "Nis");

    ResponseEntity<Map<String, Object>> approveForbidden = exchangeWithBearer(
      "/api/admin/clubs/" + clubId + "/approve",
      HttpMethod.PATCH,
      ownerToken,
      Map.of()
    );
    ResponseEntity<Map<String, Object>> rejectForbidden = exchangeWithBearer(
      "/api/admin/clubs/" + clubId + "/reject",
      HttpMethod.PATCH,
      ownerToken,
      Map.of()
    );

    assertThat(approveForbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(approveForbidden.getBody()).containsEntry("code", "FORBIDDEN");
    assertThat(rejectForbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(rejectForbidden.getBody()).containsEntry("code", "FORBIDDEN");
    assertThat(clubRepository.findById(clubId).orElseThrow().getStatus())
      .isEqualTo(ClubStatus.PENDING_APPROVAL);
  }

  @Test
  void adminGetsNotFoundForUnknownClub() {
    String adminToken = createAdminToken("missing-club-admin@example.com");

    ResponseEntity<Map<String, Object>> notFound = exchangeWithBearer(
      "/api/admin/clubs/" + UUID.randomUUID() + "/approve",
      HttpMethod.PATCH,
      adminToken,
      Map.of()
    );

    assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(notFound.getBody()).containsEntry("code", "RESOURCE_NOT_FOUND");
  }

  @Test
  void publicClubListContainsOnlyApprovedClubsWithoutAuthentication() {
    String ownerToken = register("public-list-owner@example.com", "OWNER");
    UUID approvedClubId = createClub(ownerToken, "Alfa padel", "Beograd");
    createClub(ownerToken, "Klub na cekanju", "Nis");
    approveClub(approvedClubId);

    ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
      "/api/clubs",
      HttpMethod.GET,
      HttpEntity.EMPTY,
      JSON_LIST
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).singleElement().satisfies(club -> {
      assertThat(club)
        .containsEntry("id", approvedClubId.toString())
        .containsEntry("name", "Alfa padel")
        .containsEntry("status", "APPROVED");
    });
  }

  @Test
  void publicClubDetailsExposeApprovedClubAndHidePendingClub() {
    String ownerToken = register("public-details-owner@example.com", "OWNER");
    UUID approvedClubId = createClub(ownerToken, "Javni klub", "Novi Sad");
    UUID pendingClubId = createClub(ownerToken, "Skriveni klub", "Subotica");
    approveClub(approvedClubId);

    ResponseEntity<Map<String, Object>> approved = restTemplate.exchange(
      "/api/clubs/" + approvedClubId,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      JSON_OBJECT
    );
    ResponseEntity<Map<String, Object>> pending = restTemplate.exchange(
      "/api/clubs/" + pendingClubId,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      JSON_OBJECT
    );

    assertThat(approved.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(approved.getBody())
      .containsEntry("id", approvedClubId.toString())
      .containsEntry("status", "APPROVED");
    assertThat(pending.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(pending.getBody()).containsEntry("code", "RESOURCE_NOT_FOUND");
  }

  @Test
  void publicCourtsContainOnlyActiveCourtsOfApprovedClub() {
    String ownerToken = register("public-courts-owner@example.com", "OWNER");
    UUID approvedClubId = createClub(ownerToken, "Klub sa terenima", "Beograd");
    UUID pendingClubId = createClub(ownerToken, "Neodobreni klub", "Nis");
    Club approvedClub = approveClub(approvedClubId);
    Club pendingClub = clubRepository.findById(pendingClubId).orElseThrow();
    Court activeCourt = courtRepository.saveAndFlush(
      new Court(approvedClub, "Centralni teren", CourtType.PANORAMIC, true)
    );
    courtRepository.saveAndFlush(
      new Court(approvedClub, "Teren u renoviranju", CourtType.STANDARD, false)
    );
    courtRepository.saveAndFlush(
      new Court(pendingClub, "Skriveni teren", CourtType.SINGLE, true)
    );

    ResponseEntity<List<Map<String, Object>>> courts = restTemplate.exchange(
      "/api/clubs/" + approvedClubId + "/courts",
      HttpMethod.GET,
      HttpEntity.EMPTY,
      JSON_LIST
    );
    ResponseEntity<Map<String, Object>> pendingClubCourts = restTemplate.exchange(
      "/api/clubs/" + pendingClubId + "/courts",
      HttpMethod.GET,
      HttpEntity.EMPTY,
      JSON_OBJECT
    );

    assertThat(courts.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(courts.getBody()).singleElement().satisfies(court -> {
      assertThat(court)
        .containsEntry("id", activeCourt.getId().toString())
        .containsEntry("name", "Centralni teren")
        .containsEntry("type", "PANORAMIC")
        .containsEntry("active", true);
    });
    assertThat(pendingClubCourts.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(pendingClubCourts.getBody()).containsEntry("code", "RESOURCE_NOT_FOUND");
  }

  private String register(String email, String role) {
    ResponseEntity<Map<String, Object>> registration = restTemplate.exchange(
      "/api/auth/register",
      HttpMethod.POST,
      jsonEntity(Map.of(
        "email", email,
        "password", "strongPassword123",
        "firstName", "Test",
        "lastName", "User",
        "role", role
      )),
      JSON_OBJECT
    );

    assertThat(registration.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(registration.getBody()).isNotNull();
    return registration.getBody().get("accessToken").toString();
  }

  private UUID createClub(String accessToken, String name, String city) {
    ResponseEntity<Map<String, Object>> created = exchangeWithBearer(
      "/api/owner/clubs",
      HttpMethod.POST,
      accessToken,
      Map.of("name", name, "city", city)
    );

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(created.getBody()).containsEntry("status", "PENDING_APPROVAL");
    return UUID.fromString(created.getBody().get("id").toString());
  }

  private String createAdminToken(String email) {
    User admin = userRepository.saveAndFlush(new User(
      email,
      "unused-password-hash",
      "Admin",
      "User",
      UserRole.ADMIN
    ));
    return jwtTokenService.createAccessToken(
      admin.getId(),
      admin.getEmail(),
      List.of(admin.getRole().name())
    ).token();
  }

  private Club approveClub(UUID clubId) {
    Club club = clubRepository.findById(clubId).orElseThrow();
    club.approve();
    return clubRepository.saveAndFlush(club);
  }

  private ResponseEntity<Map<String, Object>> exchangeWithBearer(
    String path,
    HttpMethod method,
    String accessToken,
    Map<String, Object> body
  ) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
    return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), JSON_OBJECT);
  }

  private HttpEntity<Map<String, Object>> jsonEntity(Map<String, Object> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "application/json");
    return new HttpEntity<>(body, headers);
  }
}
