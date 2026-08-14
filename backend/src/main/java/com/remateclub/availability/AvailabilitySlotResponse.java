package com.remateclub.availability;

import java.math.BigDecimal;
import java.time.Instant;

public record AvailabilitySlotResponse(
  Instant startAt,
  Instant endAt,
  boolean available,
  BigDecimal price,
  String currency
) {
}
