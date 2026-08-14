package com.remateclub.club;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/clubs")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClubController {

  private final AdminClubService adminClubService;

  public AdminClubController(AdminClubService adminClubService) {
    this.adminClubService = adminClubService;
  }

  @GetMapping("/pending")
  List<ClubResponse> findPending() {
    return adminClubService.findPending();
  }

  @PatchMapping("/{clubId}/approve")
  ClubResponse approve(@PathVariable UUID clubId) {
    return adminClubService.approve(clubId);
  }

  @PatchMapping("/{clubId}/reject")
  ClubResponse reject(@PathVariable UUID clubId) {
    return adminClubService.reject(clubId);
  }
}
