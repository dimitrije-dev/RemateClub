package com.remateclub.court;

import com.remateclub.club.Club;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
  name = "courts",
  indexes = {
    @Index(name = "idx_courts_club_id", columnList = "club_id"),
    @Index(name = "idx_courts_club_active", columnList = "club_id, active"),
    @Index(name = "idx_courts_type", columnList = "type")
  }
)
public class Court {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "club_id", nullable = false, updatable = false)
  private Club club;

  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private CourtType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CourtEnvironment environment;

  @Column(nullable = false)
  private boolean active;

  @Column(name = "hourly_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal hourlyPrice;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Court() {
  }

  public Court(Club club, String name, CourtType type, boolean active) {
    this(club, name, type, CourtEnvironment.INDOOR, active, new BigDecimal("3000.00"));
  }

  public Court(
    Club club,
    String name,
    CourtType type,
    boolean active,
    BigDecimal hourlyPrice
  ) {
    this(club, name, type, CourtEnvironment.INDOOR, active, hourlyPrice);
  }

  public Court(
    Club club,
    String name,
    CourtType type,
    CourtEnvironment environment,
    boolean active,
    BigDecimal hourlyPrice
  ) {
    this.club = club;
    this.name = name;
    this.type = type;
    this.environment = environment;
    this.active = active;
    this.hourlyPrice = hourlyPrice;
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

  public UUID getId() {
    return id;
  }

  public Club getClub() {
    return club;
  }

  public String getName() {
    return name;
  }

  public CourtType getType() {
    return type;
  }

  public CourtEnvironment getEnvironment() {
    return environment;
  }

  public boolean isActive() {
    return active;
  }

  public BigDecimal getHourlyPrice() {
    return hourlyPrice;
  }

  public void update(String name, CourtType type, boolean active, BigDecimal hourlyPrice) {
    update(name, type, environment, active, hourlyPrice);
  }

  public void update(
    String name,
    CourtType type,
    CourtEnvironment environment,
    boolean active,
    BigDecimal hourlyPrice
  ) {
    this.name = name;
    this.type = type;
    this.environment = environment;
    this.active = active;
    this.hourlyPrice = hourlyPrice;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
