package com.hiberadar.domain.eligibility.controller;

import com.hiberadar.domain.eligibility.dto.EligibilityCheckRequest;
import com.hiberadar.domain.eligibility.dto.EligibilityCheckResponse;
import com.hiberadar.domain.eligibility.service.EligibilityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eligibility")
public class EligibilityCheckController {

    private final EligibilityService eligibilityService;

    public EligibilityCheckController(EligibilityService eligibilityService) {
        this.eligibilityService = eligibilityService;
    }

    @PostMapping("/check")
    public EligibilityCheckResponse check(@Valid @RequestBody EligibilityCheckRequest req) {
        return eligibilityService.check(req);
    }
}
