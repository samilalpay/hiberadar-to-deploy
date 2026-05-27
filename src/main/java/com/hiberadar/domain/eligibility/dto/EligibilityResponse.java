package com.hiberadar.domain.eligibility.dto;

import java.math.BigDecimal;
import java.util.List;

public record EligibilityResponse(
        Long id,
        Long grantId,
        List<String> applicantTypes,
        Integer minCompanyAgeMonths,
        Integer minEmployees,
        Integer maxEmployees,
        BigDecimal minTurnover,
        BigDecimal maxTurnover,
        Integer trlMin,
        Integer trlMax,
        List<String> requiredCountryCodes,
        Boolean cofundingRequired,
        Integer cofundingRate,
        String notes
) {}
