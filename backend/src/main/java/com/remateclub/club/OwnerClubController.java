package com.remateclub.club;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/owner/clubs")
@PreAuthorize("hasRole('OWNER')")
public class OwnerClubController {

  private final OwnerClubService ownerClubService;

  public OwnerClubController(OwnerClubService ownerClubService) {
    this.ownerClubService = ownerClubService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  ClubResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ClubRequest request) {
    return ownerClubService.create(subject(jwt), request);
  }

  @PutMapping("/{clubId}")
  ClubResponse update(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID clubId,
    @Valid @RequestBody ClubRequest request
  ) {
    return ownerClubService.update(subject(jwt), clubId, request);
  }

  @GetMapping
  List<ClubResponse> findOwn(@AuthenticationPrincipal Jwt jwt) {
    return ownerClubService.findOwn(subject(jwt));
  }

  private UUID subject(Jwt jwt) {
    try {
      return UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      throw new BadCredentialsException("Invalid token subject", exception);
    }
  }
}
