package com.remateclub.clubimage;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/owner/clubs/{clubId}/images")
@PreAuthorize("hasRole('OWNER')")
public class OwnerClubImageController {

  private final ClubImageService imageService;

  public OwnerClubImageController(ClubImageService imageService) {
    this.imageService = imageService;
  }

  @GetMapping
  List<ClubImageResponse> findOwn(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID clubId) {
    return imageService.findOwn(subject(jwt), clubId);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  ClubImageResponse upload(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID clubId,
    @RequestParam MultipartFile file,
    @RequestParam String altText,
    @RequestParam(defaultValue = "false") boolean cover
  ) {
    return imageService.upload(subject(jwt), clubId, file, altText, cover);
  }

  @PatchMapping("/{imageId}/cover")
  ClubImageResponse makeCover(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID clubId,
    @PathVariable UUID imageId
  ) {
    return imageService.makeCover(subject(jwt), clubId, imageId);
  }

  @DeleteMapping("/{imageId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(
    @AuthenticationPrincipal Jwt jwt,
    @PathVariable UUID clubId,
    @PathVariable UUID imageId
  ) {
    imageService.delete(subject(jwt), clubId, imageId);
  }

  private UUID subject(Jwt jwt) {
    try {
      return UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      throw new BadCredentialsException("Invalid token subject", exception);
    }
  }
}
