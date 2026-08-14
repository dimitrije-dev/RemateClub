package com.remateclub.clubimage;

import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PublicClubImageController {

  private final ClubImageService imageService;

  public PublicClubImageController(ClubImageService imageService) {
    this.imageService = imageService;
  }

  @GetMapping("/clubs/{clubId}/images")
  List<ClubImageResponse> findPublic(@PathVariable UUID clubId) {
    return imageService.findPublic(clubId);
  }

  @GetMapping("/club-images/{imageId}/content")
  ResponseEntity<org.springframework.core.io.Resource> content(@PathVariable UUID imageId) {
    ClubImageService.ImageContent content = imageService.loadContent(imageId);
    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(content.contentType()))
      .cacheControl(CacheControl.noCache())
      .header(
        HttpHeaders.CONTENT_DISPOSITION,
        ContentDisposition.inline().filename(content.filename(), StandardCharsets.UTF_8).build().toString()
      )
      .body(content.resource());
  }
}
