package com.remateclub.booking;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourtBlockRepository extends JpaRepository<CourtBlock, UUID> {

  @Query("""
    select (count(block) > 0)
    from CourtBlock block
    where block.court.id = :courtId
      and block.startAt < :endAt
      and block.endAt > :startAt
    """)
  boolean existsOverlapping(
    @Param("courtId") UUID courtId,
    @Param("startAt") Instant startAt,
    @Param("endAt") Instant endAt
  );

  @Query("""
    select block
    from CourtBlock block
    where block.court.id = :courtId
      and block.startAt < :endAt
      and block.endAt > :startAt
    order by block.startAt
    """)
  List<CourtBlock> findOverlapping(
    @Param("courtId") UUID courtId,
    @Param("startAt") Instant startAt,
    @Param("endAt") Instant endAt
  );
}
