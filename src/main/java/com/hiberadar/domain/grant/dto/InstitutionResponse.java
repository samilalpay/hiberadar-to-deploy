package com.hiberadar.domain.grant.dto;

import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import java.time.LocalDateTime;

public record InstitutionResponse(
        Long id,
        String name,
        String shortCode,
        String logoUrl,
        InstitutionScope scope,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
