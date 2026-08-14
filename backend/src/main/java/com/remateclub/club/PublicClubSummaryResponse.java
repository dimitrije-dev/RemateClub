package com.remateclub.club;

import com.remateclub.court.Court;
import com.remateclub.court.CourtEnvironment;
import com.remateclub.court.CourtType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record PublicClubSummaryResponse(
  UUID id,
  UUID ownerId,
  String name,
  String city,
  String address,
  BigDecimal latitude,
  BigDecimal longitude,
  BigDecimal averageRating,
  int reviewCount,
  ClubStatus status,
  Instant createdAt,
  Instant updatedAt,
  String coverImageUrl,
  BigDecimal minimumHourlyPrice,
  Set<CourtType> courtTypes,
  Set<CourtEnvironment> environments,
  Instant earliestAvailableAt
) {

  static PublicClubSummaryResponse from(
    Club club,
    List<Court> courts,
    Instant earliestAvailableAt
  ) {
    ClubResponse details = ClubResponse.from(club);
    BigDecimal minimumPrice = courts.stream()
      .map(Court::getHourlyPrice)
      .min(BigDecimal::compareTo)
      .orElse(null);
    return new PublicClubSummaryResponse(
      details.id(),
      details.ownerId(),
      details.name(),
      details.city(),
      details.address(),
      details.latitude(),
      details.longitude(),
      details.averageRating(),
      details.reviewCount(),
      details.status(),
      details.createdAt(),
      details.updatedAt(),
      details.coverImageUrl(),
      minimumPrice,
      courts.stream().map(Court::getType).collect(Collectors.toUnmodifiableSet()),
      courts.stream().map(Court::getEnvironment).collect(Collectors.toUnmodifiableSet()),
      earliestAvailableAt
    );
  }
}
