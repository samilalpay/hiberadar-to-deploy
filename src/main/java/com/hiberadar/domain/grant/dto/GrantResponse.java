package com.hiberadar.domain.grant.dto;

import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GrantResponse(
        Long id,
        String title,

        Long sourceId,
        String sourceName,

        GrantStatus status,
        String officialUrl,
        String providerName,
        String programName,
        String referenceCode,
        String summaryShort,

        LocalDate publishedAt,
        LocalDate deadlineAt,

        String currency,
        BigDecimal fundingMin,
        BigDecimal fundingMax,

        String countryCode,
        InstitutionScope scope,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
