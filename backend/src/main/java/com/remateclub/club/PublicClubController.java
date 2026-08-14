package com.remateclub.club;

import com.remateclub.court.CourtResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs")
public class PublicClubController {

  private final PublicClubService publicClubService;

  public PublicClubController(PublicClubService publicClubService) {
    this.publicClubService = publicClubService;
  }

  @GetMapping
  List<ClubResponse> findAll() {
    return publicClubService.findApproved();
  }

  @GetMapping("/{clubId}")
  ClubResponse findById(@PathVariable UUID clubId) {
    return publicClubService.findApprovedById(clubId);
  }

  @GetMapping("/{clubId}/courts")
  List<CourtResponse> findCourts(@PathVariable UUID clubId) {
    return publicClubService.findActiveCourts(clubId);
  }
}
