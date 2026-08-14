package com.remateclub.court;

import com.remateclub.club.Club;
import com.remateclub.club.ClubRepository;
import com.remateclub.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerCourtService {

  private final ClubRepository clubRepository;
  private final CourtRepository courtRepository;

  public OwnerCourtService(ClubRepository clubRepository, CourtRepository courtRepository) {
    this.clubRepository = clubRepository;
    this.courtRepository = courtRepository;
  }

  @Transactional(readOnly = true)
  public List<CourtResponse> findAll(UUID ownerId, UUID clubId) {
    requireOwnedClub(ownerId, clubId);
    return courtRepository.findAllByClubIdOrderByNameAsc(clubId)
      .stream()
      .map(CourtResponse::from)
      .toList();
  }

  @Transactional
  public CourtResponse create(UUID ownerId, UUID clubId, OwnerCourtRequest request) {
    Club club = requireOwnedClub(ownerId, clubId);
    Court court = new Court(
      club,
      request.name().trim(),
      request.type(),
      request.active(),
      request.hourlyPrice()
    );
    return CourtResponse.from(courtRepository.saveAndFlush(court));
  }

  @Transactional
  public CourtResponse update(
    UUID ownerId,
    UUID clubId,
    UUID courtId,
    OwnerCourtRequest request
  ) {
    requireOwnedClub(ownerId, clubId);
    Court court = courtRepository.findById(courtId)
      .filter(found -> found.getClub().getId().equals(clubId))
      .orElseThrow(() -> new ResourceNotFoundException("Court not found"));
    court.update(
      request.name().trim(),
      request.type(),
      request.active(),
      request.hourlyPrice()
    );
    return CourtResponse.from(court);
  }

  private Club requireOwnedClub(UUID ownerId, UUID clubId) {
    Club club = clubRepository.findById(clubId)
      .orElseThrow(() -> new ResourceNotFoundException("Club not found"));
    if (!club.getOwner().getId().equals(ownerId)) {
      throw new AccessDeniedException("Owner cannot manage another owner's club");
    }
    return club;
  }
}
