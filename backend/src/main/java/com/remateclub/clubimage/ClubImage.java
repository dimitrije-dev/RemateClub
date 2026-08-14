package com.remateclub.clubimage;

import com.remateclub.club.Club;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "club_images", indexes = @Index(name = "idx_club_images_club_id", columnList = "club_id"))
public class ClubImage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "club_id", nullable = false, updatable = false)
  private Club club;

  @Column(name = "storage_key", nullable = false, unique = true, length = 120, updatable = false)
  private String storageKey;

  @Column(name = "original_filename", nullable = false, length = 255, updatable = false)
  private String originalFilename;

  @Column(name = "content_type", nullable = false, length = 40, updatable = false)
  private String contentType;

  @Column(name = "file_size", nullable = false, updatable = false)
  private long fileSize;

  @Column(nullable = false, updatable = false)
  private int width;

  @Column(nullable = false, updatable = false)
  private int height;

  @Column(name = "alt_text", nullable = false, length = 180)
  private String altText;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Column(name = "is_cover", nullable = false)
  private boolean cover;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected ClubImage() {
  }

  public ClubImage(
    Club club,
    String storageKey,
    String originalFilename,
    String contentType,
    long fileSize,
    int width,
    int height,
    String altText,
    int sortOrder,
    boolean cover
  ) {
    this.club = club;
    this.storageKey = storageKey;
    this.originalFilename = originalFilename;
    this.contentType = contentType;
    this.fileSize = fileSize;
    this.width = width;
    this.height = height;
    this.altText = altText;
    this.sortOrder = sortOrder;
    this.cover = cover;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }

  public void makeCover() {
    this.cover = true;
  }

  public void removeCover() {
    this.cover = false;
  }

  public UUID getId() { return id; }
  public Club getClub() { return club; }
  public String getStorageKey() { return storageKey; }
  public String getOriginalFilename() { return originalFilename; }
  public String getContentType() { return contentType; }
  public long getFileSize() { return fileSize; }
  public int getWidth() { return width; }
  public int getHeight() { return height; }
  public String getAltText() { return altText; }
  public int getSortOrder() { return sortOrder; }
  public boolean isCover() { return cover; }
  public Instant getCreatedAt() { return createdAt; }
}
