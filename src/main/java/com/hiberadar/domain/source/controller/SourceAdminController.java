package com.hiberadar.domain.source.controller;

import com.hiberadar.domain.source.dto.CreateSourceRequest;
import com.hiberadar.domain.source.dto.SourceResponse;
import com.hiberadar.domain.source.dto.UpdateSourceRequest;
import com.hiberadar.domain.source.service.SourceService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sources")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class SourceAdminController {

    private final SourceService sourceService;

    public SourceAdminController(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    @PostMapping
    public SourceResponse create(@Valid @RequestBody CreateSourceRequest req) {
        return sourceService.create(req);
    }

    @PutMapping("/{id}")
    public SourceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateSourceRequest req) {
        return sourceService.update(id, req);
    }
}
