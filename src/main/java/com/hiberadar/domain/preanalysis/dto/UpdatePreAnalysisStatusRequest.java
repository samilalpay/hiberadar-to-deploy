package com.hiberadar.domain.preanalysis.dto;

import com.hiberadar.domain.preanalysis.entity.enums.PreAnalysisStatus;
import jakarta.validation.constraints.NotNull;

public record UpdatePreAnalysisStatusRequest(
        @NotNull PreAnalysisStatus status,
        String reviewNote,
        String reportSummary
) {
}
