package com.hiberadar.domain.grant.controller;

import com.hiberadar.domain.grant.dto.InstitutionResponse;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.service.InstitutionQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/institutions")
public class PublicInstitutionController {

    private final InstitutionQueryService institutionQueryService;

    public PublicInstitutionController(InstitutionQueryService institutionQueryService) {
        this.institutionQueryService = institutionQueryService;
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
}
