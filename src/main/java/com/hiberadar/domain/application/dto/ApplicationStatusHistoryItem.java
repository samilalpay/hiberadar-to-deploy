package com.hiberadar.domain.application.dto;

import com.hiberadar.domain.application.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationStatusHistoryItem(
        ApplicationStatus fromStatus,
        ApplicationStatus toStatus,
        String note,
        LocalDateTime changedAt,
        String changedByUsername
) {
}
