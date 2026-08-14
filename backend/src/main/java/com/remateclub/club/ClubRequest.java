package com.remateclub.club;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ClubRequest(
  @NotBlank @Size(max = 160) String name,
  @NotBlank @Size(max = 120) String city,
  @Size(max = 255) String address,
  @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
  @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
) {
}
