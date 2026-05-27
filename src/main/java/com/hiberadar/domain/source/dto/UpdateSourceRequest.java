package com.hiberadar.domain.source.dto;

import com.hiberadar.domain.source.entity.enums.SourceCategory;
import com.hiberadar.domain.source.entity.enums.SourceScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateSourceRequest(
        @NotBlank String name,
        @NotNull SourceCategory category,
        @NotNull SourceScope scope,
        String countryCode,
        String officialUrl,
        String notes,
        Boolean active
) {}
