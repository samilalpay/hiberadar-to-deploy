package com.hiberadar.domain.ingest.controller;

import com.hiberadar.domain.ingest.dto.IngestMetricsResponse;
import com.hiberadar.domain.ingest.dto.IngestRunResponse;
import com.hiberadar.domain.ingest.dto.IngestStatusResponse;
import com.hiberadar.domain.ingest.service.GrantIngestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ingest")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class IngestAdminController {

    private final GrantIngestService grantIngestService;

    public IngestAdminController(GrantIngestService grantIngestService) {
        this.grantIngestService = grantIngestService;
    }

    @PostMapping("/run")
    public IngestRunResponse runNow(Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "UNKNOWN";
        return grantIngestService.runNow("MANUAL:" + username);
    }

    @GetMapping("/status")
    public IngestStatusResponse status() {
        return grantIngestService.status();
    }

    @GetMapping("/jobs")
    public Page<IngestRunResponse> jobs(Pageable pageable) {
        return grantIngestService.listRuns(pageable);
    }

    @GetMapping("/metrics")
    public IngestMetricsResponse metrics() {
        return grantIngestService.metrics();
    }
}
