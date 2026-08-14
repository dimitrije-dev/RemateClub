package com.remateclub.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.remateclub.auth.RefreshTokenRepository;
import com.remateclub.club.Club;
import com.remateclub.club.ClubRepository;
import com.remateclub.court.Court;
import com.remateclub.court.CourtRepository;
import com.remateclub.court.CourtType;
import com.remateclub.security.JwtTokenService;
import com.remateclub.user.User;
import com.remateclub.user.UserRepository;
import com.remateclub.user.UserRole;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
class BookingIntegrationTest {

  private static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Belgrade");
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
    registry.add("remate-club.security.jwt.secret", () -> "booking-test-secret-with-at-least-32-bytes");
  }

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private CourtBlockRepository courtBlockRepository;

  @Autowired
  private CourtRepository courtRepository;

  @Autowired
  private ClubRepository clubRepository;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JwtTokenService jwtTokenService;

  @BeforeEach
  void cleanDatabase() {
    bookingRepository.deleteAll();
    courtBlockRepository.deleteAll();
    refreshTokenRepository.deleteAll();
    courtRepository.deleteAll();
    clubRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void createCalculatesPriceAndMyBookingsAndDetailsReturnPersistedBooking() {
    Fixture fixture = createFixture();
    Instant startAt = futureDate().atTime(10, 0).atZone(BUSINESS_ZONE).toInstant();
    Instant endAt = startAt.plusSeconds(2 * 60 * 60);

    ResponseEntity<Map<String, Object>> created = createBooking(
      fixture.playerToken(), fixture.court().getId(), startAt, endAt
    );

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(created.getBody())
      .containsEntry("playerId", fixture.player().getId().toString())
      .containsEntry("courtId", fixture.court().getId().toString())
      .containsEntry("status", "CONFIRMED")
      .containsEntry("currency", "RSD");
    assertThat(((Number) created.getBody().get("totalPrice")).doubleValue()).isEqualTo(4800.0);
    UUID bookingId = UUID.fromString(created.getBody().get("id").toString());

    ResponseEntity<List<Map<String, Object>>> mine = getListWithBearer(
      "/api/bookings/me", fixture.playerToken()
    );
    assertThat(mine.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mine.getBody()).singleElement().satisfies(booking ->
      assertThat(booking).containsEntry("id", bookingId.toString())
    );

    ResponseEntity<Map<String, Object>> details = getObjectWithBearer(
      "/api/bookings/" + bookingId, fixture.playerToken()
    );
    assertThat(details.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(details.getBody()).containsEntry("id", bookingId.toString());
  }

  @Test
  void ownerBookingsContainOnlyBookingsForOwnedClubs() {
    Fixture fixture = createFixture();
    Instant startAt = futureDate().atTime(11, 0).atZone(BUSINESS_ZONE).toInstant();
    UUID bookingId = UUID.fromString(createBooking(
      fixture.playerToken(), fixture.court().getId(), startAt, startAt.plusSeconds(3600)
    ).getBody().get("id").toString());

    ResponseEntity<List<Map<String, Object>>> owned = getListWithBearer(
      "/api/owner/bookings", fixture.ownerToken()
    );
    User otherOwner = createUser("other-booking-owner@example.com", UserRole.OWNER);
    ResponseEntity<List<Map<String, Object>>> otherOwners = getListWithBearer(
      "/api/owner/bookings", token(otherOwner)
    );

    assertThat(owned.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(owned.getBody()).singleElement().satisfies(booking ->
      assertThat(booking).containsEntry("id", bookingId.toString())
    );
    assertThat(otherOwners.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(otherOwners.getBody()).isEmpty();
  }

  @Test
  void bookingDetailsAndCancellationFollowPlayerOwnerAdminRules() {
    Fixture fixture = createFixture();
    User otherPlayer = createUser("other-player@example.com", UserRole.PLAYER);
    User admin = createUser("booking-admin@example.com", UserRole.ADMIN);
    String otherPlayerToken = token(otherPlayer);
    String adminToken = token(admin);
    Instant startAt = futureDate().atTime(13, 0).atZone(BUSINESS_ZONE).toInstant();
    UUID bookingId = UUID.fromString(createBooking(
      fixture.playerToken(), fixture.court().getId(), startAt, startAt.plusSeconds(3600)
    ).getBody().get("id").toString());

    ResponseEntity<Map<String, Object>> forbidden = getObjectWithBearer(
      "/api/bookings/" + bookingId, otherPlayerToken
    );
    ResponseEntity<Map<String, Object>> ownerDetails = getObjectWithBearer(
      "/api/bookings/" + bookingId, fixture.ownerToken()
    );
    ResponseEntity<Map<String, Object>> adminDetails = getObjectWithBearer(
      "/api/bookings/" + bookingId, adminToken
    );

    assertThat(forbidden.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(forbidden.getBody()).containsEntry("code", "FORBIDDEN");
    assertThat(ownerDetails.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(adminDetails.getStatusCode()).isEqualTo(HttpStatus.OK);

    ResponseEntity<Map<String, Object>> cancelled = exchangeWithBearer(
      "/api/bookings/" + bookingId + "/cancel",
      HttpMethod.PATCH,
      fixture.ownerToken(),
      Map.of()
    );
    assertThat(cancelled.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(cancelled.getBody()).containsEntry("status", "CANCELLED");

    ResponseEntity<Map<String, Object>> replacement = createBooking(
      otherPlayerToken, fixture.court().getId(), startAt, startAt.plusSeconds(3600)
    );
    assertThat(replacement.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void overlappingBookingAndCourtBlockReturnSpecificConflictResponse() {
    Fixture fixture = createFixture();
    User secondPlayer = createUser("conflict-player@example.com", UserRole.PLAYER);
    String secondPlayerToken = token(secondPlayer);
    Instant bookingStart = futureDate().atTime(15, 0).atZone(BUSINESS_ZONE).toInstant();
    createBooking(
      fixture.playerToken(), fixture.court().getId(), bookingStart, bookingStart.plusSeconds(3600)
    );

    ResponseEntity<Map<String, Object>> bookingConflict = createBooking(
      secondPlayerToken,
      fixture.court().getId(),
      bookingStart.plusSeconds(1800),
      bookingStart.plusSeconds(5400)
    );

    Instant blockStart = futureDate().atTime(18, 0).atZone(BUSINESS_ZONE).toInstant();
    courtBlockRepository.saveAndFlush(new CourtBlock(
      fixture.court(), blockStart, blockStart.plusSeconds(3600), "Odrzavanje"
    ));
    ResponseEntity<Map<String, Object>> blockConflict = createBooking(
      secondPlayerToken,
      fixture.court().getId(),
      blockStart.plusSeconds(900),
      blockStart.plusSeconds(2700)
    );

    assertUnavailableConflict(bookingConflict);
    assertUnavailableConflict(blockConflict);
    assertThat(bookingRepository.count()).isEqualTo(1);
  }

  @Test
  void availabilityMarksConfirmedBookingsAndCourtBlocksUnavailable() {
    Fixture fixture = createFixture();
    LocalDate date = futureDate();
    Instant bookedStart = date.atTime(10, 0).atZone(BUSINESS_ZONE).toInstant();
    Instant blockedStart = date.atTime(12, 0).atZone(BUSINESS_ZONE).toInstant();
    createBooking(
      fixture.playerToken(), fixture.court().getId(), bookedStart, bookedStart.plusSeconds(3600)
    );
    courtBlockRepository.saveAndFlush(new CourtBlock(
      fixture.court(), blockedStart, blockedStart.plusSeconds(3600), "Privatni termin"
    ));

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
      "/api/courts/" + fixture.court().getId() + "/availability?date=" + date,
      HttpMethod.GET,
      HttpEntity.EMPTY,
      JSON_OBJECT
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .containsEntry("courtId", fixture.court().getId().toString())
      .containsEntry("date", date.toString())
      .containsEntry("zoneId", "Europe/Belgrade");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> slots = (List<Map<String, Object>>) response.getBody().get("slots");
    assertThat(slots).hasSize(15);
    assertThat(slotAt(slots, bookedStart)).containsEntry("available", false);
    assertThat(slotAt(slots, bookedStart.plusSeconds(3600))).containsEntry("available", true);
    assertThat(slotAt(slots, blockedStart)).containsEntry("available", false);
  }

  @Test
  void playerCanRescheduleConfirmedBookingAndPriceIsRecalculated() {
    Fixture fixture = createFixture();
    Instant originalStart = futureDate().atTime(9, 0).atZone(BUSINESS_ZONE).toInstant();
    UUID bookingId = UUID.fromString(createBooking(
      fixture.playerToken(), fixture.court().getId(), originalStart, originalStart.plusSeconds(3600)
    ).getBody().get("id").toString());
    Instant newStart = futureDate().atTime(17, 0).atZone(BUSINESS_ZONE).toInstant();

    ResponseEntity<Map<String, Object>> rescheduled = exchangeWithBearer(
      "/api/bookings/" + bookingId + "/reschedule",
      HttpMethod.PATCH,
      fixture.playerToken(),
      Map.of(
        "startAt", newStart.toString(),
        "endAt", newStart.plusSeconds(7200).toString()
      )
    );

    assertThat(rescheduled.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(rescheduled.getBody())
      .containsEntry("id", bookingId.toString())
      .containsEntry("startAt", newStart.toString())
      .containsEntry("status", "CONFIRMED");
    assertThat(((Number) rescheduled.getBody().get("totalPrice")).doubleValue()).isEqualTo(4800.0);
    Booking persisted = bookingRepository.findById(bookingId).orElseThrow();
    assertThat(persisted.getStartAt()).isEqualTo(newStart);
    assertThat(persisted.getEndAt()).isEqualTo(newStart.plusSeconds(7200));
  }

  @Test
  void startedBookingCannotBeCancelledOrRescheduled() {
    Fixture fixture = createFixture();
    Instant startedAt = Instant.now().minusSeconds(60);
    Booking booking = bookingRepository.saveAndFlush(new Booking(
      fixture.player(),
      fixture.court(),
      startedAt,
      startedAt.plusSeconds(3600),
      new BigDecimal("2400.00")
    ));
    Instant newStart = futureDate().atTime(19, 0).atZone(BUSINESS_ZONE).toInstant();

    ResponseEntity<Map<String, Object>> cancellation = exchangeWithBearer(
      "/api/bookings/" + booking.getId() + "/cancel",
      HttpMethod.PATCH,
      fixture.playerToken(),
      Map.of()
    );
    ResponseEntity<Map<String, Object>> reschedule = exchangeWithBearer(
      "/api/bookings/" + booking.getId() + "/reschedule",
      HttpMethod.PATCH,
      fixture.playerToken(),
      Map.of(
        "startAt", newStart.toString(),
        "endAt", newStart.plusSeconds(3600).toString()
      )
    );

    assertThat(cancellation.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(reschedule.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
      .isEqualTo(BookingStatus.CONFIRMED);
  }

  @Test
  void twoConcurrentRequestsForSameCourtAndTimeCreateOnlyOneBooking() throws Exception {
    Fixture fixture = createFixture();
    User secondPlayer = createUser("concurrent-player@example.com", UserRole.PLAYER);
    String secondPlayerToken = token(secondPlayer);
    Instant startAt = futureDate().atTime(20, 0).atZone(BUSINESS_ZONE).toInstant();
    Instant endAt = startAt.plusSeconds(3600);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);

    try {
      List<Future<Integer>> futures = new ArrayList<>();
      for (String accessToken : List.of(fixture.playerToken(), secondPlayerToken)) {
        futures.add(executor.submit(() -> {
          start.await();
          return createBooking(
            accessToken, fixture.court().getId(), startAt, endAt
          ).getStatusCode().value();
        }));
      }

      start.countDown();
      List<Integer> statuses = new ArrayList<>();
      for (Future<Integer> future : futures) {
        statuses.add(future.get());
      }

      assertThat(statuses).containsExactlyInAnyOrder(201, 409);
      assertThat(bookingRepository.count()).isEqualTo(1);
      assertThat(bookingRepository.findAll().getFirst().getStatus())
        .isEqualTo(BookingStatus.CONFIRMED);
    } finally {
      executor.shutdownNow();
    }
  }

  private Fixture createFixture() {
    User owner = createUser("fixture-owner@example.com", UserRole.OWNER);
    User player = createUser("fixture-player@example.com", UserRole.PLAYER);
    Club club = new Club(owner, "Booking klub", "Beograd");
    club.approve();
    club = clubRepository.saveAndFlush(club);
    Court court = courtRepository.saveAndFlush(new Court(
      club,
      "Teren 1",
      CourtType.PANORAMIC,
      true,
      new BigDecimal("2400.00")
    ));
    return new Fixture(owner, player, court, token(owner), token(player));
  }

  private User createUser(String email, UserRole role) {
    return userRepository.saveAndFlush(new User(
      email,
      "unused-password-hash",
      "Test",
      "User",
      role
    ));
  }

  private String token(User user) {
    return jwtTokenService.createAccessToken(
      user.getId(), user.getEmail(), List.of(user.getRole().name())
    ).token();
  }

  private ResponseEntity<Map<String, Object>> createBooking(
    String accessToken,
    UUID courtId,
    Instant startAt,
    Instant endAt
  ) {
    return exchangeWithBearer(
      "/api/bookings",
      HttpMethod.POST,
      accessToken,
      Map.of(
        "courtId", courtId.toString(),
        "startAt", startAt.toString(),
        "endAt", endAt.toString(),
        "totalPrice", 1
      )
    );
  }

  private ResponseEntity<Map<String, Object>> getObjectWithBearer(String path, String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), JSON_OBJECT);
  }

  private ResponseEntity<List<Map<String, Object>>> getListWithBearer(
    String path,
    String accessToken
  ) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), JSON_LIST);
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

  private void assertUnavailableConflict(ResponseEntity<Map<String, Object>> response) {
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).containsEntry("code", "BOOKING_TIME_UNAVAILABLE");
  }

  private Map<String, Object> slotAt(List<Map<String, Object>> slots, Instant startAt) {
    return slots.stream()
      .filter(slot -> startAt.toString().equals(slot.get("startAt")))
      .findFirst()
      .orElseThrow();
  }

  private LocalDate futureDate() {
    return LocalDate.now(BUSINESS_ZONE).plusDays(2);
  }

  private record Fixture(
    User owner,
    User player,
    Court court,
    String ownerToken,
    String playerToken
  ) {
  }
}
