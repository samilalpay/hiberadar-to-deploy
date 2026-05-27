package com.hiberadar.domain.preanalysis.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePreAnalysisRequest(
        @NotBlank String activityArea,
        String machinePark,
        String investmentPlan,
        String rdExperience,
        String exportStatus,
        String financialCapacity,
        String note
) {
}
