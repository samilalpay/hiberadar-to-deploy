package com.hiberadar.domain.application.dto;

import com.hiberadar.domain.application.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApplicationDetailResponse(
        Long id,
        Long grantId,
        String grantTitle,
        Long firmUserId,
        String firmUsername,
        ApplicationStatus status,
        LocalDateTime submittedAt,
        LocalDateTime decidedAt,
        String decisionNote,
        List<ApplicationStatusHistoryItem> history
) {
}
