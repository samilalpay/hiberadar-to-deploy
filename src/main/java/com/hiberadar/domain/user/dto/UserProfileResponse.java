package com.hiberadar.domain.user.dto;

import java.math.BigDecimal;

public record UserProfileResponse(
        String username,
        String email,
        String role,
        boolean profileCompleted,
        String applicantType,
        Integer companyAgeMonths,
        Integer employees,
        String countryCode,
        Boolean cofundingAvailable,
        Integer cofundingRate,
        String sector,
        String activityArea,
        BigDecimal turnover,
        String naceCodes
) {}
