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
    this.name = name;
    this.city = city;
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
