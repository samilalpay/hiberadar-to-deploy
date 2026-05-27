package com.hiberadar.domain.application.dto;

import com.hiberadar.domain.application.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationStatusHistoryResponse(
        Long id,
        Long applicationId,
        ApplicationStatus fromStatus,
        ApplicationStatus toStatus,
        String note,
        Long changedByUserId,
        LocalDateTime changedAt
) {}
