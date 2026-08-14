package com.remateclub.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClubRequest(
  @NotBlank @Size(max = 160) String name,
  @NotBlank @Size(max = 120) String city
) {
}
