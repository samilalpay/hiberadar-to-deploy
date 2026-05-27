package com.hiberadar.domain.grant.mapper;

import com.hiberadar.domain.grant.dto.GrantDetailDto;
import com.hiberadar.domain.grant.dto.GrantListItemDto;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;

public class GrantMapper {

    private GrantMapper() {
    }

    public static GrantListItemDto toListItem(Grant g) {
        GrantListItemDto dto = new GrantListItemDto();
        dto.id = g.getId();
        dto.title = g.getTitle();
        dto.status = g.getStatus();
        dto.providerName = g.getProviderName();
        dto.programName = g.getProgramName();
        dto.referenceCode = g.getReferenceCode();
        dto.summaryShort = g.getSummaryShort();
        dto.publishedAt = g.getPublishedAt();
        dto.deadlineAt = g.getDeadlineAt();
        dto.naceCode = g.getNaceCode();
        dto.countryCode = g.getCountryCode();
        dto.scope = g.getScope();
        dto.currency = g.getCurrency();
        dto.fundingMin = g.getFundingMin();
        dto.fundingMax = g.getFundingMax();
        dto.clickable = g.getStatus() == GrantStatus.PUBLISHED;
        return dto;
    }

    public static GrantDetailDto toDetail(Grant g) {
        GrantDetailDto dto = new GrantDetailDto();
        dto.id = g.getId();
        dto.title = g.getTitle();
        dto.status = g.getStatus();
        dto.officialUrl = g.getOfficialUrl();

        dto.providerName = g.getProviderName();
        dto.programName = g.getProgramName();
        dto.referenceCode = g.getReferenceCode();

        dto.summaryShort = g.getSummaryShort();
        dto.adminQuickInfo = g.getAdminQuickInfo();

        dto.publishedAt = g.getPublishedAt();
        dto.deadlineAt = g.getDeadlineAt();
        dto.naceCode = g.getNaceCode();

        dto.currency = g.getCurrency();
        dto.fundingMin = g.getFundingMin();
        dto.fundingMax = g.getFundingMax();

        dto.countryCode = g.getCountryCode();
        dto.scope = g.getScope();
        return dto;
    }

    // ✅ GrantAdminService benim önerdiğim "toResponse" ismini çağırıyordu.
    // Var olan DTO’nla uyumlu şekilde alias yapıyoruz:
    public static GrantDetailDto toResponse(Grant g) {
        return toDetail(g);
    }
}
