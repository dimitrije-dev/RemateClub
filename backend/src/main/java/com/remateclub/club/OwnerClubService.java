package com.remateclub.club;

import com.remateclub.common.exception.ResourceNotFoundException;
import com.remateclub.user.User;
import com.remateclub.user.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerClubService {

  private final ClubRepository clubRepository;
  private final UserRepository userRepository;

  public OwnerClubService(ClubRepository clubRepository, UserRepository userRepository) {
    this.clubRepository = clubRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public ClubResponse create(UUID ownerId, ClubRequest request) {
    User owner = userRepository.findById(ownerId)
      .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Club club = new Club(owner, request.name().trim(), request.city().trim());
    club.update(
      request.name().trim(),
      request.city().trim(),
      normalize(request.address()),
      request.latitude(),
      request.longitude()
    );
    return ClubResponse.from(clubRepository.saveAndFlush(club));
  }

  @Transactional
  public ClubResponse update(UUID ownerId, UUID clubId, ClubRequest request) {
    Club club = clubRepository.findById(clubId)
      .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

    if (!club.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("Owner cannot manage another owner's club");
    }

    club.update(
      request.name().trim(),
      request.city().trim(),
      normalize(request.address()),
      request.latitude(),
      request.longitude()
    );
    return ClubResponse.from(club);
  }

  @Transactional(readOnly = true)
  public List<ClubResponse> findOwn(UUID ownerId) {
    return clubRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId)
      .stream()
      .map(ClubResponse::from)
      .toList();
  }

  private String normalize(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }
}
