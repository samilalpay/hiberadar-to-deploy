package com.hiberadar.domain.eligibility.dto;

import java.util.List;

public record EligibilityCheckResponse(
        boolean eligible,
        List<String> reasons
) {}
