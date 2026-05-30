package com.hiberadar.domain.user.dto;

public record AdminFirmListItem(
        Long id,
        String companyName,
        String username,
        String email,
        boolean profileCompleted,
        String sector,
        String countryCode,
        Integer employees,
        String companyLogoUrl,
        boolean active
) {
}
