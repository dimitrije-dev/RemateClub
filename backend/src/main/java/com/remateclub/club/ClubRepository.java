package com.remateclub.club;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClubRepository extends JpaRepository<Club, UUID> {

  List<Club> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

  List<Club> findAllByStatusOrderByNameAsc(ClubStatus status);

  Optional<Club> findByIdAndStatus(UUID id, ClubStatus status);
}
