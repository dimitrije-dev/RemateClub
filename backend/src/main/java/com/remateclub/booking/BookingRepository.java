package com.remateclub.booking;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

  @Query("""
    select (count(booking) > 0)
    from Booking booking
    where booking.court.id = :courtId
      and booking.status = :status
      and booking.startAt < :endAt
      and booking.endAt > :startAt
    """)
  boolean existsOverlapping(
    @Param("courtId") UUID courtId,
    @Param("status") BookingStatus status,
    @Param("startAt") Instant startAt,
    @Param("endAt") Instant endAt
  );

  @Query("""
    select (count(booking) > 0)
    from Booking booking
    where booking.court.id = :courtId
      and booking.id <> :bookingId
      and booking.status = :status
      and booking.startAt < :endAt
      and booking.endAt > :startAt
    """)
  boolean existsOverlappingExcluding(
    @Param("courtId") UUID courtId,
    @Param("bookingId") UUID bookingId,
    @Param("status") BookingStatus status,
    @Param("startAt") Instant startAt,
    @Param("endAt") Instant endAt
  );

  @Query("""
    select booking
    from Booking booking
    where booking.court.id = :courtId
      and booking.status = :status
      and booking.startAt < :endAt
      and booking.endAt > :startAt
    order by booking.startAt
    """)
  List<Booking> findOverlapping(
    @Param("courtId") UUID courtId,
    @Param("status") BookingStatus status,
    @Param("startAt") Instant startAt,
    @Param("endAt") Instant endAt
  );

  List<Booking> findAllByPlayerIdOrderByStartAtDesc(UUID playerId);

  List<Booking> findAllByCourtClubOwnerIdOrderByStartAtDesc(UUID ownerId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select booking from Booking booking where booking.id = :bookingId")
  Optional<Booking> findByIdForUpdate(@Param("bookingId") UUID bookingId);
}
