package com.hiberadar.domain.grant.controller;

import com.hiberadar.domain.grant.dto.CreateInstitutionRequest;
import com.hiberadar.domain.grant.dto.InstitutionResponse;
import com.hiberadar.domain.grant.entity.Institution;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.service.InstitutionCommandService;
import com.hiberadar.domain.grant.service.InstitutionLogoStorageService;
import com.hiberadar.domain.grant.service.InstitutionQueryService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/institutions")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class AdminInstitutionController {

    private final InstitutionQueryService institutionQueryService;
    private final InstitutionCommandService institutionCommandService;
    private final InstitutionLogoStorageService logoStorageService;

    public AdminInstitutionController(
            InstitutionQueryService institutionQueryService,
            InstitutionCommandService institutionCommandService,
            InstitutionLogoStorageService logoStorageService) {
        this.institutionQueryService = institutionQueryService;
        this.institutionCommandService = institutionCommandService;
        this.logoStorageService = logoStorageService;
    }

    @GetMapping
    public List<InstitutionResponse> list(@RequestParam(required = false) InstitutionScope scope) {
        if (scope != null) {
            return institutionQueryService.getByScope(scope);
        }
        return institutionQueryService.getAll();
    }

    @GetMapping("/{id}")
    public InstitutionResponse getById(@PathVariable Long id) {
        return institutionQueryService.getById(id);
    }

    @PostMapping
    public InstitutionResponse create(@Valid @RequestBody CreateInstitutionRequest req) {
        Institution institution = institutionCommandService.create(req);
        return institutionQueryService.getById(institution.getId());
    }

    @PutMapping("/{id}")
    public InstitutionResponse update(
            @PathVariable Long id,
            @Valid @RequestBody CreateInstitutionRequest req) {
        Institution institution = institutionCommandService.update(id, req);
        return institutionQueryService.getById(institution.getId());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        institutionCommandService.delete(id);
    }

    @PostMapping(value = "/{id}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public InstitutionResponse uploadLogo(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        String logoUrl = logoStorageService.storeLogo(id, file);
        Institution institution = institutionCommandService.updateLogo(id, logoUrl);
        return institutionQueryService.getById(institution.getId());
    }
}
