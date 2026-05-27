package com.hiberadar.domain.grant.controller;

import com.hiberadar.domain.grant.dto.AdminCreateGrantRequest;
import com.hiberadar.domain.grant.dto.AdminUpdateGrantRequest;
import com.hiberadar.domain.grant.dto.GrantDetailDto;
import com.hiberadar.domain.grant.dto.GrantListItemDto;
import com.hiberadar.domain.grant.dto.UpdateGrantStatusRequest;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.service.GrantAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/grants")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class AdminGrantController {

    private final GrantAdminService grantAdminService;

    public AdminGrantController(GrantAdminService grantAdminService) {
        this.grantAdminService = grantAdminService;
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
            @PageableDefault(size = 24) Pageable pageable) {
        InstitutionScope parsedScope = null;
        if (scope != null && !scope.isBlank()) {
            try {
                parsedScope = InstitutionScope.valueOf(scope.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                parsedScope = null;
            }
        }

        return grantAdminService.adminList(
                status,
                sourceId,
                nace,
                parsedScope,
                countryCode,
                currency,
                q,
                deadlineFrom,
                deadlineTo,
                minFunding,
                maxFunding,
                pageable);
    }

    @PostMapping
    public GrantDetailDto create(@Valid @RequestBody AdminCreateGrantRequest req) {
        return grantAdminService.create(req);
    }

    @PutMapping("/{id}")
    public GrantDetailDto update(@PathVariable Long id, @Valid @RequestBody AdminUpdateGrantRequest req) {
        return grantAdminService.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public GrantDetailDto patchStatus(@PathVariable Long id, @Valid @RequestBody UpdateGrantStatusRequest req) {
        return grantAdminService.patchStatus(id, req);
    }

    @PatchMapping(path = "/{id}/status", params = "status")
    public GrantDetailDto patchStatusByQuery(@PathVariable Long id, @RequestParam GrantStatus status) {
        return grantAdminService.patchStatus(id, new UpdateGrantStatusRequest(status));
    }

    @PatchMapping("/{id}/active")
    public GrantDetailDto setActive(@PathVariable Long id, @RequestParam boolean active) {
        return grantAdminService.setActive(id, active);
    }

    @GetMapping("/{id}")
    public GrantDetailDto get(@PathVariable Long id) {
        return grantAdminService.get(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        grantAdminService.delete(id);
    }
}
