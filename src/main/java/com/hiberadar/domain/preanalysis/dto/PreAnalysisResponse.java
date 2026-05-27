package com.hiberadar.domain.preanalysis.dto;

import java.time.LocalDateTime;

public record PreAnalysisResponse(
        Long id,
        String firmUsername,
        String activityArea,
        String machinePark,
        String investmentPlan,
        String rdExperience,
        String exportStatus,
        String financialCapacity,
        String firmNote,
        String status,
        String reviewNote,
        String reportSummary,
        String reviewedBy,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt
) {
}
