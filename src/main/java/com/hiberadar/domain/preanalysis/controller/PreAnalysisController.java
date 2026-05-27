package com.hiberadar.domain.preanalysis.controller;

import com.hiberadar.domain.preanalysis.dto.CreatePreAnalysisRequest;
import com.hiberadar.domain.preanalysis.dto.PreAnalysisResponse;
import com.hiberadar.domain.preanalysis.service.PreAnalysisService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pre-analysis")
@PreAuthorize("hasRole('FIRMA')")
public class PreAnalysisController {

    private final PreAnalysisService preAnalysisService;

    public PreAnalysisController(PreAnalysisService preAnalysisService) {
        this.preAnalysisService = preAnalysisService;
    }

    @PostMapping
    public PreAnalysisResponse create(Authentication authentication, @Valid @RequestBody CreatePreAnalysisRequest req) {
        return preAnalysisService.create(authentication.getName(), req);
    }

    @GetMapping("/me")
    public Page<PreAnalysisResponse> my(Authentication authentication, Pageable pageable) {
        return preAnalysisService.myRequests(authentication.getName(), pageable);
    }
}
