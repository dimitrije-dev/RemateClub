package com.remateclub.booking;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owner/bookings")
@PreAuthorize("hasRole('OWNER')")
public class OwnerBookingController {

  private final BookingService bookingService;

  public OwnerBookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @GetMapping
  List<BookingResponse> findOwnedClubBookings(@AuthenticationPrincipal Jwt jwt) {
    return bookingService.findForOwner(subject(jwt));
  }

  private UUID subject(Jwt jwt) {
    try {
      return UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      throw new BadCredentialsException("Invalid token subject", exception);
    }
  }
}
