package com.remateclub.court;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OwnerCourtRequest(
  @NotBlank @Size(max = 120) String name,
  @NotNull CourtType type,
  boolean active,
  @NotNull @DecimalMin(value = "0.01") BigDecimal hourlyPrice
) {
}
