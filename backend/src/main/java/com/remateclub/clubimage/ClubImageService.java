package com.remateclub.clubimage;

import com.remateclub.club.Club;
import com.remateclub.club.ClubRepository;
import com.remateclub.club.ClubStatus;
import com.remateclub.common.exception.BadRequestException;
import com.remateclub.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClubImageService {

  static final int MAX_IMAGES_PER_CLUB = 8;

  private final ClubRepository clubRepository;
  private final ClubImageRepository imageRepository;
  private final ClubImageStorage storage;

  public ClubImageService(ClubRepository clubRepository, ClubImageRepository imageRepository, ClubImageStorage storage) {
    this.clubRepository = clubRepository;
    this.imageRepository = imageRepository;
    this.storage = storage;
  }

  @Transactional(readOnly = true)
  public List<ClubImageResponse> findPublic(UUID clubId) {
    clubRepository.findByIdAndStatus(clubId, ClubStatus.APPROVED)
      .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
    return responses(clubId);
  }

  @Transactional(readOnly = true)
  public List<ClubImageResponse> findOwn(UUID ownerId, UUID clubId) {
    requireOwnedClub(ownerId, clubId);
    return responses(clubId);
  }

  @Transactional
  public ClubImageResponse upload(
    UUID ownerId,
    UUID clubId,
    MultipartFile file,
    String altText,
    boolean requestedCover
  ) {
    Club club = requireOwnedClub(ownerId, clubId);
    long imageCount = imageRepository.countByClubId(clubId);
    if (imageCount >= MAX_IMAGES_PER_CLUB) {
      throw new BadRequestException("Klub može imati najviše 8 slika");
    }
    String normalizedAlt = normalizeAltText(altText);
    StoredImage stored = storage.store(file);
    boolean cover = requestedCover || imageCount == 0;
    try {
      if (cover) imageRepository.clearCover(clubId);
      ClubImage image = new ClubImage(
        club,
        stored.storageKey(),
        normalizeFilename(file.getOriginalFilename()),
        stored.contentType(),
        stored.fileSize(),
        stored.dimensions().width(),
        stored.dimensions().height(),
        normalizedAlt,
        Math.toIntExact(imageCount),
        cover
      );
      return ClubImageResponse.from(imageRepository.saveAndFlush(image));
    } catch (RuntimeException exception) {
      storage.delete(stored.storageKey());
      throw exception;
    }
  }

  @Transactional
  public ClubImageResponse makeCover(UUID ownerId, UUID clubId, UUID imageId) {
    requireOwnedClub(ownerId, clubId);
    ClubImage image = imageRepository.findByIdAndClubId(imageId, clubId)
      .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    if (image.isCover()) {
      return ClubImageResponse.from(image);
    }
    imageRepository.clearCover(clubId);
    image.makeCover();
    return ClubImageResponse.from(imageRepository.saveAndFlush(image));
  }

  @Transactional
  public void delete(UUID ownerId, UUID clubId, UUID imageId) {
    requireOwnedClub(ownerId, clubId);
    ClubImage image = imageRepository.findByIdAndClubId(imageId, clubId)
      .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    boolean wasCover = image.isCover();
    String storageKey = image.getStorageKey();
    imageRepository.delete(image);
    imageRepository.flush();
    if (wasCover) {
      imageRepository.findAllByClubIdOrderByCoverDescSortOrderAscCreatedAtAsc(clubId)
        .stream()
        .findFirst()
        .ifPresent(next -> {
          next.makeCover();
          imageRepository.save(next);
        });
    }
    storage.delete(storageKey);
  }

  @Transactional(readOnly = true)
  public ImageContent loadContent(UUID imageId) {
    ClubImage image = imageRepository.findById(imageId)
      .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    return new ImageContent(storage.load(image.getStorageKey()), image.getContentType(), image.getOriginalFilename());
  }

  private List<ClubImageResponse> responses(UUID clubId) {
    return imageRepository.findAllByClubIdOrderByCoverDescSortOrderAscCreatedAtAsc(clubId)
      .stream()
      .map(ClubImageResponse::from)
      .toList();
  }

  private Club requireOwnedClub(UUID ownerId, UUID clubId) {
    Club club = clubRepository.findById(clubId)
      .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
    if (!club.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("Owner cannot manage another owner's club");
    }
    return club;
  }

  private String normalizeAltText(String altText) {
    if (altText == null || altText.isBlank()) {
      throw new BadRequestException("Opis slike je obavezan");
    }
    String normalized = altText.trim();
    if (normalized.length() > 180) {
      throw new BadRequestException("Opis slike može imati najviše 180 karaktera");
    }
    return normalized;
  }

  private String normalizeFilename(String filename) {
    if (filename == null || filename.isBlank()) return "club-image";
    String normalized = filename.replace('\\', '/');
    normalized = normalized.substring(normalized.lastIndexOf('/') + 1).trim();
    return normalized.isEmpty() ? "club-image" : normalized.substring(0, Math.min(normalized.length(), 255));
  }

  public record ImageContent(Resource resource, String contentType, String filename) {
  }
}
