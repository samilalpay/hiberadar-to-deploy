package com.hiberadar.domain.grant.service;

import com.hiberadar.domain.grant.dto.AdminCreateGrantRequest;
import com.hiberadar.domain.grant.dto.AdminUpdateGrantRequest;
import com.hiberadar.domain.grant.dto.GrantDetailDto;
import com.hiberadar.domain.grant.dto.GrantListItemDto;
import com.hiberadar.domain.grant.dto.UpdateGrantStatusRequest;
import com.hiberadar.domain.application.repository.ApplicationRepository;
import com.hiberadar.domain.eligibility.repository.EligibilityRuleRepository;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.mapper.GrantMapper;
import com.hiberadar.domain.grant.repository.GrantRepository;
import com.hiberadar.domain.source.entity.Source;
import com.hiberadar.domain.source.service.SourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class GrantAdminService {

    private final GrantRepository grantRepository;
    private final ApplicationRepository applicationRepository;
    private final EligibilityRuleRepository eligibilityRuleRepository;
    private final SourceService sourceService;
    private final GrantMatchNotificationService grantMatchNotificationService;

    public GrantAdminService(GrantRepository grantRepository,
            ApplicationRepository applicationRepository,
            EligibilityRuleRepository eligibilityRuleRepository,
            SourceService sourceService,
            GrantMatchNotificationService grantMatchNotificationService) {
        this.grantRepository = grantRepository;
        this.applicationRepository = applicationRepository;
        this.eligibilityRuleRepository = eligibilityRuleRepository;
        this.sourceService = sourceService;
        this.grantMatchNotificationService = grantMatchNotificationService;
    }

    @Transactional(readOnly = true)
    public Page<GrantListItemDto> adminList(
            GrantStatus status,
            Long sourceId,
            String nace,
            InstitutionScope scope,
            String countryCode,
            String currency,
            String q,
            LocalDate deadlineFrom,
            LocalDate deadlineTo,
            BigDecimal minFunding,
            BigDecimal maxFunding,
            Pageable pageable) {
        String statusStr = (status == null) ? null : status.name();
        String scopeStr = (scope == null) ? null : scope.name();

        return grantRepository.search(
                statusStr,
                sourceId,
                nace,
                scopeStr,
                countryCode,
                currency,
                q,
                deadlineFrom,
                deadlineTo,
                minFunding,
                maxFunding,
                pageable).map(GrantMapper::toListItem);
    }

    @Transactional
    public GrantDetailDto create(AdminCreateGrantRequest req) {
        Source source = sourceService.getOrCreateManualSource();
        Long sourceId = source.getId();

        // ✅ unique constraint: ux_grants_source_refcode (source_id + reference_code)
        if (req.referenceCode() != null && !req.referenceCode().isBlank()) {
            boolean existsRef = grantRepository.existsBySource_IdAndReferenceCodeIgnoreCase(
                    sourceId, req.referenceCode());
            if (existsRef) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Grant already exists for this source + referenceCode");
            }
        }

        // ✅ unique constraint: ux_grants_source_url (source_id + official_url)
        if (req.officialUrl() != null && !req.officialUrl().isBlank()) {
            boolean existsUrl = grantRepository.existsBySource_IdAndOfficialUrlIgnoreCase(
                    sourceId, req.officialUrl());
            if (existsUrl) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Grant already exists for this source + officialUrl");
            }
        }

        Grant g = new Grant();
        g.setSource(source);

        g.setTitle(req.title());
        g.setScope(req.scope());

        g.setNaceCode(req.naceCode());
        g.setCountryCode(req.countryCode());
        g.setOfficialUrl(req.officialUrl());
        g.setProviderName(req.providerName());
        g.setProgramName(req.programName());
        g.setReferenceCode(req.referenceCode());
        g.setSummaryShort(req.summaryShort());
        g.setAdminQuickInfo(req.adminQuickInfo());

        g.setDeadlineAt(req.deadlineAt());

        g.setCurrency(req.currency());
        g.setFundingMin(req.fundingMin());
        g.setFundingMax(req.fundingMax());

        // Admin create => her zaman DRAFT
        g.setStatus(GrantStatus.DRAFT);
        g.setPublishedAt(null);

        Grant saved = grantRepository.save(g);
        return GrantMapper.toResponse(saved);
    }

    @Transactional
    public GrantDetailDto update(Long id, AdminUpdateGrantRequest req) {
        Grant g = grantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found: " + id));

        // ✅ update sırasında da url/refcode unique patlatmasın (başkasının kaydı ile
        // çakışıyorsa)
        if (req.referenceCode() != null && !req.referenceCode().isBlank()) {
            boolean conflictRef = grantRepository.existsBySource_IdAndReferenceCodeIgnoreCaseAndIdNot(
                    g.getSource().getId(), req.referenceCode(), id);
            if (conflictRef) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Another grant already exists for this source + referenceCode");
            }
        }

        if (req.officialUrl() != null && !req.officialUrl().isBlank()) {
            boolean conflictUrl = grantRepository.existsBySource_IdAndOfficialUrlIgnoreCaseAndIdNot(
                    g.getSource().getId(), req.officialUrl(), id);
            if (conflictUrl) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Another grant already exists for this source + officialUrl");
            }
        }

        g.setTitle(req.title());
        g.setScope(req.scope());

        g.setNaceCode(req.naceCode());
        g.setCountryCode(req.countryCode());
        g.setOfficialUrl(req.officialUrl());
        g.setProviderName(req.providerName());
        g.setProgramName(req.programName());
        g.setReferenceCode(req.referenceCode());
        g.setSummaryShort(req.summaryShort());
        g.setAdminQuickInfo(req.adminQuickInfo());

        g.setDeadlineAt(req.deadlineAt());

        g.setCurrency(req.currency());
        g.setFundingMin(req.fundingMin());
        g.setFundingMax(req.fundingMax());

        Grant saved = grantRepository.save(g);
        return GrantMapper.toResponse(saved);
    }

    @Transactional
    public GrantDetailDto patchStatus(Long id, UpdateGrantStatusRequest req) {
        Grant g = grantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found: " + id));

        GrantStatus current = g.getStatus();
        GrantStatus target = req.status();

        if (target == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status is required");
        }

        // Idempotent patch + publishedAt backfill
        if (Objects.equals(current, target)) {
            if (current == GrantStatus.PUBLISHED && g.getPublishedAt() == null) {
                g.setPublishedAt(LocalDate.now());
                Grant saved = grantRepository.save(g);
                return GrantMapper.toResponse(saved);
            }
            return GrantMapper.toResponse(g);
        }

        // Admin panelde tum status gecislerine izin verilir.
        g.setStatus(target);
        if (target == GrantStatus.PUBLISHED) {
            if (g.getPublishedAt() == null) {
                g.setPublishedAt(LocalDate.now());
            }
            Grant saved = grantRepository.save(g);
            grantMatchNotificationService.notifyMatchingFirms(saved);
            return GrantMapper.toResponse(saved);
        }

        Grant saved = grantRepository.save(g);
        return GrantMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GrantDetailDto get(Long id) {
        Grant g = grantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found: " + id));
        return GrantMapper.toResponse(g);
    }

    @Transactional
    public void delete(Long id) {
        Grant g = grantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found: " + id));

        if (applicationRepository.existsByGrant_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This grant has applications and cannot be deleted. Use CLOSED status instead.");
        }

        if (eligibilityRuleRepository.existsByGrantId(id)) {
            eligibilityRuleRepository.deleteByGrantId(id);
        }

        grantRepository.delete(g);
    }

    @Transactional
    public GrantDetailDto setActive(Long id, boolean active) {
        Grant g = grantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found: " + id));

        if (active) {
            if (g.getStatus() == GrantStatus.DRAFT) {
                return patchStatus(id, new UpdateGrantStatusRequest(GrantStatus.PUBLISHED));
            }
            return GrantMapper.toResponse(g);
        }

        if (g.getStatus() == GrantStatus.PUBLISHED) {
            return patchStatus(id, new UpdateGrantStatusRequest(GrantStatus.CLOSED));
        }
        return GrantMapper.toResponse(g);
    }
}
