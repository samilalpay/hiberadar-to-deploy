package com.hiberadar.domain.grant.dto;

import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AdminUpdateGrantRequest(
        @NotBlank String title,
        @NotNull InstitutionScope scope,

        String naceCode,
        String countryCode,
        String officialUrl,
        String providerName,
        String programName,
        String referenceCode,
        String summaryShort,
        String adminQuickInfo,

        LocalDate deadlineAt,

        String currency,
        BigDecimal fundingMin,
        BigDecimal fundingMax
) {}
