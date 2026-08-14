package com.remateclub.booking;

import com.remateclub.court.Court;
import com.remateclub.user.User;
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
  name = "bookings",
  indexes = {
    @Index(name = "idx_bookings_court_period", columnList = "court_id, start_at, end_at"),
    @Index(name = "idx_bookings_player_start", columnList = "player_id, start_at"),
    @Index(name = "idx_bookings_status", columnList = "status")
  }
)
public class Booking {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "player_id", nullable = false, updatable = false)
  private User player;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "court_id", nullable = false, updatable = false)
  private Court court;

  @Column(name = "start_at", nullable = false, updatable = false)
  private Instant startAt;

  @Column(name = "end_at", nullable = false, updatable = false)
  private Instant endAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private BookingStatus status;

  @Column(name = "total_price", nullable = false, precision = 12, scale = 2, updatable = false)
  private BigDecimal totalPrice;

  @Column(name = "cancelled_at")
  private Instant cancelledAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected Booking() {
  }

  public Booking(
    User player,
    Court court,
    Instant startAt,
    Instant endAt,
    BigDecimal totalPrice
  ) {
    this.player = player;
    this.court = court;
    this.startAt = startAt;
    this.endAt = endAt;
    this.totalPrice = totalPrice;
    this.status = BookingStatus.CONFIRMED;
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

  public void cancel() {
    this.status = BookingStatus.CANCELLED;
    this.cancelledAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public User getPlayer() {
    return player;
  }

  public Court getCourt() {
    return court;
  }

  public Instant getStartAt() {
    return startAt;
  }

  public Instant getEndAt() {
    return endAt;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public Instant getCancelledAt() {
    return cancelledAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
