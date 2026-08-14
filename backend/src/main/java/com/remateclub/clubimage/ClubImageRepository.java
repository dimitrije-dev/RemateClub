package com.remateclub.clubimage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubImageRepository extends JpaRepository<ClubImage, UUID> {
  List<ClubImage> findAllByClubIdOrderByCoverDescSortOrderAscCreatedAtAsc(UUID clubId);
  Optional<ClubImage> findByIdAndClubId(UUID id, UUID clubId);
  Optional<ClubImage> findFirstByClubIdAndCoverTrue(UUID clubId);
  long countByClubId(UUID clubId);

  @Modifying(flushAutomatically = true, clearAutomatically = false)
  @Query("update ClubImage image set image.cover = false where image.club.id = :clubId and image.cover = true")
  int clearCover(@Param("clubId") UUID clubId);
}
