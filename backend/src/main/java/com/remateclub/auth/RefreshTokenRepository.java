package com.remateclub.auth;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findAllByUserIdAndRevokedAtIsNull(UUID userId);

  long deleteByExpiresAtBefore(Instant expiresAt);

  boolean existsByTokenHash(String tokenHash);
}
