package com.hiberadar.domain.source.controller;

import com.hiberadar.domain.source.dto.CreateSourceRequest;
import com.hiberadar.domain.source.dto.SourceResponse;
import com.hiberadar.domain.source.dto.UpdateSourceRequest;
import com.hiberadar.domain.source.service.SourceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sources")
public class SourceController {

    private final SourceService sourceService;

    public SourceController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    // Admin
    @PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
    @PostMapping
    public SourceResponse create(@Valid @RequestBody CreateSourceRequest req) {
        return sourceService.create(req);
    }

    // Public/Admin
    @GetMapping
    public List<SourceResponse> listAll() {
        return sourceService.listAll();
    }

    // Admin (edit screen için lazım)
    @GetMapping("/{id}")
    public SourceResponse get(@PathVariable Long id) {
        return sourceService.get(id);
    }

    // Admin
    @PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
    @PutMapping("/{id}")
    public SourceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateSourceRequest req) {
        return sourceService.update(id, req);
    }

    // Admin (aktif/pasif)
    @PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
    @PatchMapping("/{id}/active")
    public SourceResponse setActive(@PathVariable Long id, @RequestParam boolean active) {
        return sourceService.setActive(id, active);
    }

}
