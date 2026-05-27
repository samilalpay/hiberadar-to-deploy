package com.hiberadar.domain.eligibility.dto;

import java.math.BigDecimal;
import java.util.List;

public record UpsertEligibilityRequest(
        List<String> applicantTypes,         // ["SME","STARTUP"]
        Integer minCompanyAgeMonths,
        Integer minEmployees,
        Integer maxEmployees,
        BigDecimal minTurnover,
        BigDecimal maxTurnover,
        Integer trlMin,
        Integer trlMax,
        List<String> requiredCountryCodes,   // ["TR"] or ["TR","DE"]
        Boolean cofundingRequired,
        Integer cofundingRate,
        String notes
) {}
