package com.hiberadar.domain.application.dto;

import com.hiberadar.domain.application.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;

/**
 * Lightweight admin list row for applications.
 */
public record ApplicationAdminListItem(
        Long id,
        Long grantId,
        String grantTitle,
        Long firmUserId,
        String firmUsername,
        ApplicationStatus status,
        LocalDateTime submittedAt
) {
}
