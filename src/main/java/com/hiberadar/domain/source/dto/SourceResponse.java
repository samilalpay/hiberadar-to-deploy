package com.hiberadar.domain.source.dto;

import com.hiberadar.domain.source.entity.enums.SourceCategory;
import com.hiberadar.domain.source.entity.enums.SourceScope;

public record SourceResponse(
        Long id,
        String name,
        SourceCategory category,
        SourceScope scope,
        String countryCode,
        String officialUrl,
        String notes,
        boolean active
) {}
