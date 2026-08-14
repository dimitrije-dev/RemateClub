package com.remateclub.club;

import com.remateclub.common.exception.ResourceNotFoundException;
import com.remateclub.availability.AvailabilityService;
import com.remateclub.court.Court;
import com.remateclub.court.CourtRepository;
import com.remateclub.court.CourtResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicClubService {

  private final ClubRepository clubRepository;
  private final CourtRepository courtRepository;
  private final AvailabilityService availabilityService;

  public PublicClubService(
    ClubRepository clubRepository,
    CourtRepository courtRepository,
    AvailabilityService availabilityService
  ) {
    this.clubRepository = clubRepository;
    this.courtRepository = courtRepository;
    this.availabilityService = availabilityService;
  }

  @Transactional(readOnly = true)
  public List<PublicClubSummaryResponse> findApproved(LocalDate date) {
    return clubRepository.findAllByStatusOrderByNameAsc(ClubStatus.APPROVED)
      .stream()
      .map(club -> summary(club, date))
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

  private PublicClubSummaryResponse summary(Club club, LocalDate date) {
    List<Court> courts = courtRepository.findAllByClubIdAndActiveTrueOrderByNameAsc(club.getId());
    Instant earliest = courts.stream()
      .flatMap(court -> availabilityService.find(court.getId(), date).slots().stream())
      .filter(slot -> slot.available())
      .map(slot -> slot.startAt())
      .min(Instant::compareTo)
      .orElse(null);
    return PublicClubSummaryResponse.from(club, courts, earliest);
  }
}
