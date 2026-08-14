package com.remateclub.court;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CourtResponse(
  UUID id,
  UUID clubId,
  String name,
  CourtType type,
  CourtEnvironment environment,
  boolean active,
  BigDecimal hourlyPrice,
  Instant createdAt,
  Instant updatedAt
) {

  public static CourtResponse from(Court court) {
    return new CourtResponse(
      court.getId(),
      court.getClub().getId(),
      court.getName(),
      court.getType(),
      court.getEnvironment(),
      court.isActive(),
      court.getHourlyPrice(),
      court.getCreatedAt(),
      court.getUpdatedAt()
    );
  }
}
