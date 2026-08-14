package com.remateclub.booking;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.remateclub.club.Club;
import com.remateclub.club.ClubStatus;
import com.remateclub.court.Court;
import com.remateclub.court.CourtRepository;
import com.remateclub.user.User;
import com.remateclub.user.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private CourtBlockRepository courtBlockRepository;

  @Mock
  private CourtRepository courtRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private User player;

  @Mock
  private Court court;

  @Mock
  private Club club;

  @InjectMocks
  private BookingService bookingService;

  private UUID playerId;
  private UUID courtId;
  private Instant startAt;
  private Instant endAt;

  @BeforeEach
  void setUp() {
    playerId = UUID.randomUUID();
    courtId = UUID.randomUUID();
    startAt = Instant.now().plusSeconds(7_200);
    endAt = startAt.plusSeconds(3_600);

    when(userRepository.findById(playerId)).thenReturn(Optional.of(player));
    when(player.canAuthenticate()).thenReturn(true);
    when(courtRepository.findByIdForUpdate(courtId)).thenReturn(Optional.of(court));
    when(court.isActive()).thenReturn(true);
    when(court.getClub()).thenReturn(club);
    when(club.getStatus()).thenReturn(ClubStatus.APPROVED);
    when(court.getId()).thenReturn(courtId);
  }

  @Test
  void rejectsPeriodOverlappingConfirmedBooking() {
    when(bookingRepository.existsOverlapping(
      courtId, BookingStatus.CONFIRMED, startAt, endAt
    )).thenReturn(true);

    assertThatThrownBy(() -> bookingService.create(
      playerId, new CreateBookingRequest(courtId, startAt, endAt)
    )).isInstanceOf(BookingTimeUnavailableException.class)
      .hasMessage("Requested booking time is unavailable");

    verify(courtBlockRepository, never()).existsOverlapping(courtId, startAt, endAt);
    verify(bookingRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
  }

  @Test
  void rejectsPeriodOverlappingCourtBlock() {
    when(bookingRepository.existsOverlapping(
      courtId, BookingStatus.CONFIRMED, startAt, endAt
    )).thenReturn(false);
    when(courtBlockRepository.existsOverlapping(courtId, startAt, endAt)).thenReturn(true);

    assertThatThrownBy(() -> bookingService.create(
      playerId, new CreateBookingRequest(courtId, startAt, endAt)
    )).isInstanceOf(BookingTimeUnavailableException.class);

    verify(bookingRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
  }
}
