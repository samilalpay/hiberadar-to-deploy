package com.hiberadar.domain.eligibility.controller;

import com.hiberadar.domain.eligibility.dto.EligibilityResponse;
import com.hiberadar.domain.eligibility.dto.UpsertEligibilityRequest;
import com.hiberadar.domain.eligibility.service.EligibilityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grants/{grantId}/eligibility")
public class EligibilityController {

    private final EligibilityService eligibilityService;

    public EligibilityController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @PutMapping
    public EligibilityResponse upsert(@PathVariable Long grantId,
                                     @RequestBody UpsertEligibilityRequest req) {
        return eligibilityService.upsert(grantId, req);
    }

    @GetMapping
    public EligibilityResponse get(@PathVariable Long grantId) {
        return eligibilityService.getByGrantId(grantId);
    }
}
