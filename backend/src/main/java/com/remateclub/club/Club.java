package com.remateclub.club;

import com.remateclub.user.User;
import com.remateclub.clubimage.ClubImage;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
  name = "clubs",
  indexes = {
    @Index(name = "idx_clubs_owner_id", columnList = "owner_id"),
    @Index(name = "idx_clubs_city", columnList = "city"),
    @Index(name = "idx_clubs_status", columnList = "status")
  }
)
public class Club {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id", nullable = false, updatable = false)
  private User owner;

  @OneToMany(mappedBy = "club", fetch = FetchType.LAZY)
  @OrderBy("cover DESC, sortOrder ASC, createdAt ASC")
  private List<ClubImage> images = new ArrayList<>();

  @Column(nullable = false, length = 160)
  private String name;

  @Column(nullable = false, length = 120)
  private String city;

  @Column(length = 255)
  private String address;

  @Column(precision = 9, scale = 6)
  private BigDecimal latitude;

  @Column(precision = 9, scale = 6)
  private BigDecimal longitude;

  @Column(name = "average_rating", nullable = false, precision = 2, scale = 1)
  private BigDecimal averageRating = BigDecimal.ZERO;

  @Column(name = "review_count", nullable = false)
  private int reviewCount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ClubStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Club() {
  }

  public Club(User owner, String name, String city) {
    this.owner = owner;
    this.name = name;
    this.city = city;
    this.status = ClubStatus.PENDING_APPROVAL;
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = Instant.now();
  }

  public void update(String name, String city) {
    update(name, city, address, latitude, longitude);
  }

  public void update(
    String name,
    String city,
    String address,
    BigDecimal latitude,
    BigDecimal longitude
  ) {
    this.name = name;
    this.city = city;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public void updateRating(BigDecimal averageRating, int reviewCount) {
    this.averageRating = averageRating;
    this.reviewCount = reviewCount;
  }

  public void approve() {
    this.status = ClubStatus.APPROVED;
  }

  public void reject() {
    this.status = ClubStatus.REJECTED;
  }

  public UUID getId() {
    return id;
  }

  public User getOwner() {
    return owner;
  }

  public String getName() {
    return name;
  }

  public String getCity() {
    return city;
  }

  public String getAddress() {
    return address;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public BigDecimal getAverageRating() {
    return averageRating;
  }

  public int getReviewCount() {
    return reviewCount;
  }

  public ClubStatus getStatus() {
    return status;
  }

  public List<ClubImage> getImages() {
    return images;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
