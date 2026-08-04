package com.remateclub.common.health;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
class HealthController {

  @GetMapping
  Map<String, Object> health() {
    return Map.of(
      "status", "UP",
      "service", "remate-club-backend",
      "timestamp", Instant.now()
    );
  }
}

