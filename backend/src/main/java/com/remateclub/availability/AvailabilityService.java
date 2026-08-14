package com.remateclub.availability;

import com.remateclub.booking.Booking;
import com.remateclub.booking.BookingRepository;
import com.remateclub.booking.BookingStatus;
import com.remateclub.booking.CourtBlock;
import com.remateclub.booking.CourtBlockRepository;
import com.remateclub.club.ClubStatus;
import com.remateclub.common.exception.ResourceNotFoundException;
import com.remateclub.court.Court;
import com.remateclub.court.CourtRepository;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvailabilityService {

  static final ZoneId BUSINESS_ZONE = ZoneId.of("Europe/Belgrade");
  private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
  private static final LocalTime CLOSING_TIME = LocalTime.of(23, 0);

  private final CourtRepository courtRepository;
  private final BookingRepository bookingRepository;
  private final CourtBlockRepository courtBlockRepository;

  public AvailabilityService(
    CourtRepository courtRepository,
    BookingRepository bookingRepository,
    CourtBlockRepository courtBlockRepository
  ) {
    this.courtRepository = courtRepository;
    this.bookingRepository = bookingRepository;
    this.courtBlockRepository = courtBlockRepository;
  }

  @Transactional(readOnly = true)
  public AvailabilityResponse find(UUID courtId, LocalDate date) {
    Court court = courtRepository.findById(courtId)
      .filter(Court::isActive)
      .filter(found -> found.getClub().getStatus() == ClubStatus.APPROVED)
      .orElseThrow(() -> new ResourceNotFoundException("Court not found"));

    Instant dayStart = date.atStartOfDay(BUSINESS_ZONE).toInstant();
    Instant dayEnd = date.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
    List<Booking> bookings = bookingRepository.findOverlapping(
      courtId,
      BookingStatus.CONFIRMED,
      dayStart,
      dayEnd
    );
    List<CourtBlock> blocks = courtBlockRepository.findOverlapping(courtId, dayStart, dayEnd);
    Instant now = Instant.now();
    List<AvailabilitySlotResponse> slots = new ArrayList<>();

    for (LocalTime start = OPENING_TIME; start.isBefore(CLOSING_TIME); start = start.plusHours(1)) {
      Instant slotStart = date.atTime(start).atZone(BUSINESS_ZONE).toInstant();
      Instant slotEnd = date.atTime(start.plusHours(1)).atZone(BUSINESS_ZONE).toInstant();
      boolean available = slotStart.isAfter(now)
        && bookings.stream().noneMatch(booking -> overlaps(
          booking.getStartAt(), booking.getEndAt(), slotStart, slotEnd
        ))
        && blocks.stream().noneMatch(block -> overlaps(
          block.getStartAt(), block.getEndAt(), slotStart, slotEnd
        ));

      slots.add(new AvailabilitySlotResponse(
        slotStart,
        slotEnd,
        available,
        court.getHourlyPrice().setScale(2, RoundingMode.HALF_UP),
        "RSD"
      ));
    }

    return new AvailabilityResponse(courtId, date, BUSINESS_ZONE.getId(), slots);
  }

  private boolean overlaps(
    Instant existingStart,
    Instant existingEnd,
    Instant requestedStart,
    Instant requestedEnd
  ) {
    return existingStart.isBefore(requestedEnd) && existingEnd.isAfter(requestedStart);
  }
}
