package com.hiberadar.domain.grant.dto;

import com.hiberadar.domain.grant.entity.enums.InstitutionScope;

public record CreateInstitutionRequest(
        String name,
        String shortCode,
        String logoUrl,
        InstitutionScope scope) {
}
