package com.hiberadar.domain.grant.dto;

import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;

import java.math.BigDecimal;
import java.time.LocalDate;

public class GrantDetailDto {
    public Long id;
    public String title;
    public GrantStatus status;
    public String officialUrl;

    public String providerName;
    public String programName;
    public String referenceCode;

    public String summaryShort;
    public String adminQuickInfo;

    public LocalDate publishedAt;
    public LocalDate deadlineAt;
    public String naceCode;

    public String currency;
    public BigDecimal fundingMin;
    public BigDecimal fundingMax;

    public String countryCode;
    public InstitutionScope scope;
}
