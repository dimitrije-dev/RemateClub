package com.remateclub.booking;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('PLAYER')")
  BookingResponse create(
    @AuthenticationPrincipal Jwt jwt,
    @Valid @RequestBody CreateBookingRequest request
  ) {
    return bookingService.create(subject(jwt), request);
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('PLAYER')")
  List<BookingResponse> findMine(@AuthenticationPrincipal Jwt jwt) {
    return bookingService.findMine(subject(jwt));
  }

  @GetMapping("/{bookingId}")
  BookingResponse findById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID bookingId) {
    return bookingService.findById(subject(jwt), roles(jwt), bookingId);
  }

  @PatchMapping("/{bookingId}/cancel")
  BookingResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID bookingId) {
    return bookingService.cancel(subject(jwt), roles(jwt), bookingId);
  }

  @PatchMapping("/{bookingId}/reschedule")
  @PreAuthorize("hasRole('PLAYER')")
  BookingResponse reschedule(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID bookingId,
    @Valid @RequestBody RescheduleBookingRequest request
  ) {
    return bookingService.reschedule(subject(jwt), bookingId, request);
  }

  private UUID subject(Jwt jwt) {
    try {
      return UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      throw new BadCredentialsException("Invalid token subject", exception);
    }
  }

  private Set<String> roles(Jwt jwt) {
    List<String> roles = jwt.getClaimAsStringList("roles");
    return roles == null ? Set.of() : new HashSet<>(roles);
  }
}
