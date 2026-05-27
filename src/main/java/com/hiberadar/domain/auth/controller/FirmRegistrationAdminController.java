package com.hiberadar.domain.auth.controller;

import com.hiberadar.domain.auth.dto.FirmRegistrationAdminResponse;
import com.hiberadar.domain.auth.dto.ReviewFirmRegistrationRequest;
import com.hiberadar.domain.auth.dto.UpdateFirmRegistrationStatusRequest;
import com.hiberadar.domain.auth.entity.enums.FirmRegistrationStatus;
import com.hiberadar.domain.auth.service.FirmRegistrationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/firm-registrations")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class FirmRegistrationAdminController {

    private final FirmRegistrationService service;

    public FirmRegistrationAdminController(FirmRegistrationService service) {
        this.service = service;
    }

    @GetMapping
    public Page<FirmRegistrationAdminResponse> list(
            @RequestParam(required = false) FirmRegistrationStatus status,
            Pageable pageable
    ) {
        return service.listRequests(status, pageable);
    }

    @GetMapping("/{id}")
    public FirmRegistrationAdminResponse get(@PathVariable Long id) {
        return service.getRequest(id);
    }

    @PatchMapping("/{id}/status")
    public FirmRegistrationAdminResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFirmRegistrationStatusRequest req
    ) {
        return service.updateStatus(id, req);
    }

    @PatchMapping("/{id}/review")
    public FirmRegistrationAdminResponse review(
            @PathVariable Long id,
            @RequestParam String decidedBy,
            @Valid @RequestBody ReviewFirmRegistrationRequest req
    ) {
        UpdateFirmRegistrationStatusRequest update = new UpdateFirmRegistrationStatusRequest(
                req.decision(),
                req.note()
        );
        return service.updateStatus(id, update);
    }
}
