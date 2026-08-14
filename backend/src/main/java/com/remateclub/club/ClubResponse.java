package com.remateclub.club;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;
import com.remateclub.clubimage.ClubImage;

public record ClubResponse(
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
  String coverImageUrl
) {

  static ClubResponse from(Club club) {
    return new ClubResponse(
      club.getId(),
      club.getOwner().getId(),
      club.getName(),
      club.getCity(),
      club.getAddress(),
      club.getLatitude(),
      club.getLongitude(),
      club.getAverageRating(),
      club.getReviewCount(),
      club.getStatus(),
      club.getCreatedAt(),
      club.getUpdatedAt(),
      club.getImages().stream()
        .filter(ClubImage::isCover)
        .findFirst()
        .map(image -> "/api/club-images/" + image.getId() + "/content")
        .orElse(null)
    );
  }
}
