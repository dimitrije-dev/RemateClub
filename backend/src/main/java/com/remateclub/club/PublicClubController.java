package com.remateclub.club;

import com.remateclub.court.CourtResponse;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clubs")
public class PublicClubController {

  private final PublicClubService publicClubService;

  public PublicClubController(PublicClubService publicClubService) {
    this.publicClubService = publicClubService;
  }

  @GetMapping
  List<PublicClubSummaryResponse> findAll(
    @RequestParam(required = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
  ) {
    LocalDate requestedDate = date == null
      ? LocalDate.now(ZoneId.of("Europe/Belgrade"))
      : date;
    return publicClubService.findApproved(requestedDate);
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
