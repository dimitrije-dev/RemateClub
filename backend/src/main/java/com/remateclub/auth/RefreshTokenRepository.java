package com.remateclub.auth;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select refreshToken from RefreshToken refreshToken where refreshToken.tokenHash = :tokenHash")
  Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

  List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(UUID userId);

  long deleteByExpiresAtBefore(Instant expiresAt);

  boolean existsByTokenHash(String tokenHash);
}
