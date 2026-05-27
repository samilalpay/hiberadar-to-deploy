package com.hiberadar.domain.grant.controller;

import com.hiberadar.domain.grant.dto.GrantDetailDto;
import com.hiberadar.domain.grant.dto.GrantListItemDto;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.service.GrantQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/grants")
public class GrantQueryController {

    private final GrantQueryService grantQueryService;

    public GrantQueryController(GrantQueryService grantQueryService) {
        this.grantQueryService = grantQueryService;
    }

    @GetMapping
    public Page<GrantListItemDto> list(
            @RequestParam(required = false) GrantStatus status,
            @RequestParam(required = false) Long sourceId,
            @RequestParam(required = false) String nace,
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false, name = "q") String q,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineFrom,

            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineTo,

            @RequestParam(required = false) BigDecimal minFunding,
            @RequestParam(required = false) BigDecimal maxFunding,

            @PageableDefault(size = 10) Pageable pageable) {
        InstitutionScope parsedScope = null;
        if (scope != null && !scope.isBlank()) {
            try {
                parsedScope = InstitutionScope.valueOf(scope.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                parsedScope = null;
            }
        }

        return grantQueryService.list(
                status, sourceId, nace, parsedScope, countryCode, currency, q,
                deadlineFrom, deadlineTo, minFunding, maxFunding, pageable);
    }

    @GetMapping("/{id}")
    public GrantDetailDto detail(@PathVariable Long id) {
        return grantQueryService.detail(id);
    }

    @GetMapping("/matches/me")
    @PreAuthorize("hasRole('FIRMA')")
    public Page<GrantListItemDto> matchedForMe(@PageableDefault(size = 10) Pageable pageable,
            Authentication authentication) {
        return grantQueryService.matchedForUser(authentication.getName(), pageable);
    }
}
