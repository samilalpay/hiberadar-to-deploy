package com.hiberadar.domain.grant.mapper;

import com.hiberadar.domain.grant.dto.InstitutionResponse;
import com.hiberadar.domain.grant.entity.Institution;

public class InstitutionMapper {

    private InstitutionMapper() {
    }

    public static InstitutionResponse toResponse(Institution institution) {
        if (institution == null) {
            return null;
        }
        return new InstitutionResponse(
                institution.getId(),
                institution.getName(),
                institution.getShortCode(),
                institution.getLogoUrl(),
                institution.getScope(),
                institution.getCreatedAt(),
                institution.getUpdatedAt());
    }
}
