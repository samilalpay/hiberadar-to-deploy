package com.hiberadar.domain.grant.dto;

import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateGrantRequest(
        @NotNull Long sourceId,

        @NotBlank String title,
        @NotNull GrantStatus status,
        @NotNull InstitutionScope scope,

        String countryCode,
        String officialUrl,
        String providerName,
        String programName,
        String referenceCode,
        String summaryShort,

        LocalDate publishedAt,
        LocalDate deadlineAt,

        String currency,
        BigDecimal fundingMin,
        BigDecimal fundingMax
) {}
