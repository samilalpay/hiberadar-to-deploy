package com.hiberadar.domain.ingest.connector;

import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.source.entity.enums.SourceCategory;
import com.hiberadar.domain.source.entity.enums.SourceScope;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExternalGrantRecord(
        String sourceName,
        SourceCategory sourceCategory,
        SourceScope sourceScope,
        String sourceCountryCode,
        String sourceOfficialUrl,
        String title,
        String officialUrl,
        String referenceCode,
        String summary,
        String providerName,
        String programName,
        String naceCode,
        String countryCode,
        InstitutionScope scope,
        String currency,
        BigDecimal fundingMin,
        BigDecimal fundingMax,
        LocalDate deadlineAt,
        LocalDate publishedAt,
        GrantStatus status
) {
}
