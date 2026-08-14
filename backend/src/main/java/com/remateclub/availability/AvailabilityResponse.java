package com.remateclub.availability;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
  UUID courtId,
  LocalDate date,
  String zoneId,
  List<AvailabilitySlotResponse> slots
) {
}
