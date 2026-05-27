package com.hiberadar.domain.eligibility.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EligibilityCheckRequest(
                @NotNull Long grantId,
                String applicantType,
                Integer companyAgeMonths,
                Integer employees,
                BigDecimal turnover,
                String countryCode,
                Boolean cofundingAvailable,
                Integer cofundingRate) {
}
