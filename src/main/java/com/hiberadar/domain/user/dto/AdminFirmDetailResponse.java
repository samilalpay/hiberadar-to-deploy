package com.hiberadar.domain.user.dto;

import java.math.BigDecimal;

public record AdminFirmDetailResponse(
        Long id,
        String companyName,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone,
        String role,
        boolean profileCompleted,
        boolean active,
        String applicantType,
        Integer companyAgeMonths,
        Integer employees,
        String countryCode,
        Boolean cofundingAvailable,
        Integer cofundingRate,
        String sector,
        String activityArea,
        BigDecimal turnover,
        String naceCodes,
        String companyLogoUrl
) {
}
