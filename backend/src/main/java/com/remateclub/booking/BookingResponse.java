package com.remateclub.booking;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
  UUID id,
  UUID playerId,
  UUID courtId,
  UUID clubId,
  String courtName,
  String clubName,
  String clubCity,
  String clubAddress,
  BigDecimal clubLatitude,
  BigDecimal clubLongitude,
  Instant startAt,
  Instant endAt,
  BookingStatus status,
  BigDecimal totalPrice,
  String currency,
  Instant cancelledAt,
  Instant createdAt,
  Instant updatedAt
) {

  static BookingResponse from(Booking booking) {
    return new BookingResponse(
      booking.getId(),
      booking.getPlayer().getId(),
      booking.getCourt().getId(),
      booking.getCourt().getClub().getId(),
      booking.getCourt().getName(),
      booking.getCourt().getClub().getName(),
      booking.getCourt().getClub().getCity(),
      booking.getCourt().getClub().getAddress(),
      booking.getCourt().getClub().getLatitude(),
      booking.getCourt().getClub().getLongitude(),
      booking.getStartAt(),
      booking.getEndAt(),
      booking.getStatus(),
      booking.getTotalPrice(),
      "RSD",
      booking.getCancelledAt(),
      booking.getCreatedAt(),
      booking.getUpdatedAt()
    );
  }
}
