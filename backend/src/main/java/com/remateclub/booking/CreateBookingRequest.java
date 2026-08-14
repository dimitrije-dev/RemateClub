package com.remateclub.booking;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateBookingRequest(
  @NotNull UUID courtId,
  @NotNull Instant startAt,
  @NotNull Instant endAt
) {
}
