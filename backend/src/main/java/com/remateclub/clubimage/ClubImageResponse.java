package com.remateclub.clubimage;

import java.time.Instant;
import java.util.UUID;

public record ClubImageResponse(
  UUID id,
  UUID clubId,
  String url,
  String originalFilename,
  String contentType,
  long fileSize,
  int width,
  int height,
  String altText,
  int sortOrder,
  boolean cover,
  Instant createdAt
) {
  static ClubImageResponse from(ClubImage image) {
    return new ClubImageResponse(
      image.getId(),
      image.getClub().getId(),
      "/api/club-images/" + image.getId() + "/content",
      image.getOriginalFilename(),
      image.getContentType(),
      image.getFileSize(),
      image.getWidth(),
      image.getHeight(),
      image.getAltText(),
      image.getSortOrder(),
      image.isCover(),
      image.getCreatedAt()
    );
  }
}
