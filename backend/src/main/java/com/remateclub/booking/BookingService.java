package com.remateclub.booking;

import com.remateclub.club.ClubStatus;
import com.remateclub.common.exception.BadRequestException;
import com.remateclub.common.exception.ConflictException;
import com.remateclub.common.exception.ResourceNotFoundException;
import com.remateclub.court.Court;
import com.remateclub.court.CourtRepository;
import com.remateclub.user.User;
import com.remateclub.user.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

  private final BookingRepository bookingRepository;
  private final CourtBlockRepository courtBlockRepository;
  private final CourtRepository courtRepository;
  private final UserRepository userRepository;

  public BookingService(
    BookingRepository bookingRepository,
    CourtBlockRepository courtBlockRepository,
    CourtRepository courtRepository,
    UserRepository userRepository
  ) {
    this.bookingRepository = bookingRepository;
    this.courtBlockRepository = courtBlockRepository;
    this.courtRepository = courtRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public BookingResponse create(UUID playerId, CreateBookingRequest request) {
    validatePeriod(request.startAt(), request.endAt());

    User player = userRepository.findById(playerId)
      .filter(User::canAuthenticate)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Court court = courtRepository.findByIdForUpdate(request.courtId())
      .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

    if (!court.isActive() || court.getClub().getStatus() != ClubStatus.APPROVED) {
      throw new ResourceNotFoundException("Court not found");
    }

    if (bookingRepository.existsOverlapping(
      court.getId(),
      BookingStatus.CONFIRMED,
      request.startAt(),
      request.endAt()
    ) || courtBlockRepository.existsOverlapping(
      court.getId(),
      request.startAt(),
      request.endAt()
    )) {
      throw new BookingTimeUnavailableException();
    }

    Booking booking = new Booking(
      player,
      court,
      request.startAt(),
      request.endAt(),
      calculatePrice(court.getHourlyPrice(), request.startAt(), request.endAt())
    );
    return BookingResponse.from(bookingRepository.saveAndFlush(booking));
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> findMine(UUID playerId) {
    return bookingRepository.findAllByPlayerIdOrderByStartAtDesc(playerId)
      .stream()
      .map(BookingResponse::from)
      .toList();
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> findForOwner(UUID ownerId) {
    return bookingRepository.findAllByCourtClubOwnerIdOrderByStartAtDesc(ownerId)
      .stream()
      .map(BookingResponse::from)
      .toList();
  }

  @Transactional(readOnly = true)
  public BookingResponse findById(UUID actorId, Set<String> roles, UUID bookingId) {
    Booking booking = bookingRepository.findById(bookingId)
      .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    checkAccess(booking, actorId, roles);
    return BookingResponse.from(booking);
  }

  @Transactional
  public BookingResponse cancel(UUID actorId, Set<String> roles, UUID bookingId) {
    Booking booking = bookingRepository.findByIdForUpdate(bookingId)
      .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
    checkAccess(booking, actorId, roles);

    if (booking.getStatus() != BookingStatus.CONFIRMED) {
      throw new ConflictException("Only confirmed bookings can be cancelled");
    }
    if (!booking.getStartAt().isAfter(Instant.now())) {
      throw new ConflictException("A booking can only be cancelled before it starts");
    }

    booking.cancel();
    return BookingResponse.from(booking);
  }

  @Transactional
  public BookingResponse reschedule(
    UUID playerId,
    UUID bookingId,
    RescheduleBookingRequest request
  ) {
    validatePeriod(request.startAt(), request.endAt());
    Booking booking = bookingRepository.findByIdForUpdate(bookingId)
      .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

    if (!booking.getPlayer().getId().equals(playerId)) {
      throw new AccessDeniedException("Booking access is denied");
    }
    if (booking.getStatus() != BookingStatus.CONFIRMED) {
      throw new ConflictException("Only confirmed bookings can be rescheduled");
    }
    if (!booking.getStartAt().isAfter(Instant.now())) {
      throw new ConflictException("A booking can only be rescheduled before it starts");
    }

    Court court = courtRepository.findByIdForUpdate(booking.getCourt().getId())
      .orElseThrow(() -> new ResourceNotFoundException("Court not found"));
    if (!court.isActive() || court.getClub().getStatus() != ClubStatus.APPROVED) {
      throw new ResourceNotFoundException("Court not found");
    }
    if (bookingRepository.existsOverlappingExcluding(
      court.getId(),
      bookingId,
      BookingStatus.CONFIRMED,
      request.startAt(),
      request.endAt()
    ) || courtBlockRepository.existsOverlapping(
      court.getId(),
      request.startAt(),
      request.endAt()
    )) {
      throw new BookingTimeUnavailableException();
    }

    booking.reschedule(
      request.startAt(),
      request.endAt(),
      calculatePrice(court.getHourlyPrice(), request.startAt(), request.endAt())
    );
    return BookingResponse.from(booking);
  }

  private void validatePeriod(Instant startAt, Instant endAt) {
    if (!startAt.isBefore(endAt)) {
      throw new BadRequestException("Booking start must be before booking end");
    }
    if (!startAt.isAfter(Instant.now())) {
      throw new BadRequestException("Booking start must be in the future");
    }
  }

  private BigDecimal calculatePrice(BigDecimal hourlyPrice, Instant startAt, Instant endAt) {
    long seconds = Duration.between(startAt, endAt).toSeconds();
    return hourlyPrice
      .multiply(BigDecimal.valueOf(seconds))
      .divide(BigDecimal.valueOf(3600), 2, RoundingMode.HALF_UP);
  }

  private void checkAccess(Booking booking, UUID actorId, Set<String> roles) {
    boolean isPlayer = booking.getPlayer().getId().equals(actorId);
    boolean isOwner = roles.contains("OWNER")
      && booking.getCourt().getClub().getOwner().getId().equals(actorId);
    boolean isAdmin = roles.contains("ADMIN");

    if (!isPlayer && !isOwner && !isAdmin) {
      throw new AccessDeniedException("Booking access is denied");
    }
  }
}
