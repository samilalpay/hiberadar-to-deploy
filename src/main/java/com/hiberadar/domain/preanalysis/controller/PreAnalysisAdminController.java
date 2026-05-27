package com.hiberadar.domain.preanalysis.controller;

import com.hiberadar.domain.preanalysis.dto.PreAnalysisResponse;
import com.hiberadar.domain.preanalysis.dto.UpdatePreAnalysisStatusRequest;
import com.hiberadar.domain.preanalysis.entity.enums.PreAnalysisStatus;
import com.hiberadar.domain.preanalysis.service.PreAnalysisService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin/pre-analysis")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class PreAnalysisAdminController {

    private final PreAnalysisService preAnalysisService;

    public PreAnalysisAdminController(PreAnalysisService preAnalysisService) {
        this.preAnalysisService = preAnalysisService;
    }

    @GetMapping
    public Page<PreAnalysisResponse> list(@RequestParam(required = false) PreAnalysisStatus status, Pageable pageable) {
        return preAnalysisService.adminList(status, pageable);
    }

    @PatchMapping("/{id}")
    public PreAnalysisResponse update(@PathVariable Long id,
                                      @Valid @RequestBody UpdatePreAnalysisStatusRequest req,
                                      Principal principal) {
        String admin = principal != null ? principal.getName() : "";
        return preAnalysisService.adminUpdate(id, admin, req);
    }
}
