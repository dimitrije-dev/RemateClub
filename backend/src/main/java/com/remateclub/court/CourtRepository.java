package com.remateclub.court;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourtRepository extends JpaRepository<Court, UUID> {

  List<Court> findAllByClubIdAndActiveTrueOrderByNameAsc(UUID clubId);

  List<Court> findAllByClubIdOrderByNameAsc(UUID clubId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select court from Court court join fetch court.club where court.id = :courtId")
  Optional<Court> findByIdForUpdate(@Param("courtId") UUID courtId);
}
