package com.remateclub.club;

import com.remateclub.common.exception.ResourceNotFoundException;
import com.remateclub.court.CourtRepository;
import com.remateclub.court.CourtResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicClubService {

  private final ClubRepository clubRepository;
  private final CourtRepository courtRepository;

  public PublicClubService(ClubRepository clubRepository, CourtRepository courtRepository) {
    this.clubRepository = clubRepository;
    this.courtRepository = courtRepository;
  }

  @Transactional(readOnly = true)
  public List<ClubResponse> findApproved() {
    return clubRepository.findAllByStatusOrderByNameAsc(ClubStatus.APPROVED)
      .stream()
      .map(ClubResponse::from)
      .toList();
  }

  @Transactional(readOnly = true)
  public ClubResponse findApprovedById(UUID clubId) {
    return ClubResponse.from(findApprovedClub(clubId));
  }

  @Transactional(readOnly = true)
  public List<CourtResponse> findActiveCourts(UUID clubId) {
    findApprovedClub(clubId);
    return courtRepository.findAllByClubIdAndActiveTrueOrderByNameAsc(clubId)
      .stream()
      .map(CourtResponse::from)
      .toList();
  }

  private Club findApprovedClub(UUID clubId) {
    return clubRepository.findByIdAndStatus(clubId, ClubStatus.APPROVED)
      .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
  }
}
