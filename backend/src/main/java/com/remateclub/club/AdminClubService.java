package com.remateclub.club;

import com.remateclub.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminClubService {

  private final ClubRepository clubRepository;

  public AdminClubService(ClubRepository clubRepository) {
    this.clubRepository = clubRepository;
  }

  @Transactional
  public ClubResponse approve(UUID clubId) {
    Club club = findClub(clubId);
    club.approve();
    return ClubResponse.from(club);
  }

  @Transactional
  public ClubResponse reject(UUID clubId) {
    Club club = findClub(clubId);
    club.reject();
    return ClubResponse.from(club);
  }

  @Transactional(readOnly = true)
  public List<ClubResponse> findPending() {
    return clubRepository.findAllByStatusOrderByNameAsc(ClubStatus.PENDING_APPROVAL)
      .stream()
      .map(ClubResponse::from)
      .toList();
  }

  private Club findClub(UUID clubId) {
    return clubRepository.findById(clubId)
      .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
  }
}
