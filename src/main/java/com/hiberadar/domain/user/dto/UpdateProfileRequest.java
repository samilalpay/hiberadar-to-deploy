package com.hiberadar.domain.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public record UpdateProfileRequest(
                @NotBlank String companyName,
                @NotBlank String applicantType,
                @NotNull @Min(0) Integer companyAgeMonths,
                @NotNull @Min(0) Integer employees,
                @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$") String countryCode,
                @NotNull Boolean cofundingAvailable,
                @NotNull @Min(0) @Max(100) Integer cofundingRate,
                @NotBlank String sector,
                @NotBlank String activityArea,
                @NotNull @DecimalMin("0.0") BigDecimal turnover,
                @NotNull String naceCodes) {
}
