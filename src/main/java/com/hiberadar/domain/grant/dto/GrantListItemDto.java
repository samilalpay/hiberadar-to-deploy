package com.hiberadar.domain.grant.dto;

import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GrantListItemDto {
    public Long id;
    public String title;
    public GrantStatus status;
    public String providerName;
    public String programName;
    public String referenceCode;
    public String summaryShort;
    public LocalDate publishedAt;
    public LocalDate deadlineAt;
    public String naceCode;
    public String countryCode;
    public InstitutionScope scope;
    public String currency;
    public BigDecimal fundingMin;
    public BigDecimal fundingMax;
    public boolean clickable;
    public Integer matchScore;
    public List<String> matchReasons;
}
