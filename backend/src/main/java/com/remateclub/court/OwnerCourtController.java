package com.remateclub.court;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/clubs/{clubId}/courts")
@PreAuthorize("hasRole('OWNER')")
public class OwnerCourtController {

  private final OwnerCourtService ownerCourtService;

  public OwnerCourtController(OwnerCourtService ownerCourtService) {
    this.ownerCourtService = ownerCourtService;
  }

  @GetMapping
  List<CourtResponse> findAll(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID clubId) {
    return ownerCourtService.findAll(subject(jwt), clubId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  CourtResponse create(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID clubId,
    @Valid @RequestBody OwnerCourtRequest request
  ) {
    return ownerCourtService.create(subject(jwt), clubId, request);
  }

  @PutMapping("/{courtId}")
  CourtResponse update(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID clubId,
    @PathVariable UUID courtId,
    @Valid @RequestBody OwnerCourtRequest request
  ) {
    return ownerCourtService.update(subject(jwt), clubId, courtId, request);
  }

  private UUID subject(Jwt jwt) {
    try {
      return UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      throw new BadCredentialsException("Invalid token subject", exception);
    }
  }
}
