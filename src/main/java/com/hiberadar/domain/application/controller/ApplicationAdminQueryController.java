package com.hiberadar.domain.application.controller;

import com.hiberadar.domain.application.dto.ApplicationResponse;
import com.hiberadar.domain.application.dto.ApplicationStatusHistoryResponse;
import com.hiberadar.domain.application.entity.enums.ApplicationStatus;
import com.hiberadar.domain.application.service.ApplicationQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/applications")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class ApplicationAdminQueryController {

    private final ApplicationQueryService applicationQueryService;

    public ApplicationAdminQueryController(ApplicationQueryService applicationQueryService) {
        this.applicationQueryService = applicationQueryService;
    }

    @GetMapping
    public Page<ApplicationResponse> list(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long grantId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String firmUsername,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        String keyword = (firmUsername != null && !firmUsername.isBlank()) ? firmUsername : q;
        return applicationQueryService.adminList(status, grantId, keyword, pageable);
    }

    @GetMapping("/{id}")
    public ApplicationResponse detail(@PathVariable Long id) {
        return applicationQueryService.adminDetail(id);
    }

    @GetMapping("/{id}/history")
    public List<ApplicationStatusHistoryResponse> history(@PathVariable Long id) {
        return applicationQueryService.adminHistory(id);
    }
}
