package com.remateclub.booking;

import com.remateclub.court.Court;
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
@Table(
  name = "court_blocks",
  indexes = {
    @Index(name = "idx_court_blocks_court_period", columnList = "court_id, start_at, end_at")
  }
)
public class CourtBlock {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "court_id", nullable = false, updatable = false)
  private Court court;

  @Column(name = "start_at", nullable = false, updatable = false)
  private Instant startAt;

  @Column(name = "end_at", nullable = false, updatable = false)
  private Instant endAt;

  @Column(length = 300)
  private String reason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected CourtBlock() {
  }

  public CourtBlock(Court court, Instant startAt, Instant endAt, String reason) {
    this.court = court;
    this.startAt = startAt;
    this.endAt = endAt;
    this.reason = reason;
  }

  @PrePersist
  void prePersist() {
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
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

  public String getReason() {
    return reason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
