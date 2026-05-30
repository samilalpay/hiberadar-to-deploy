package com.hiberadar.domain.auth.dto;

import com.hiberadar.domain.auth.entity.enums.FirmRegistrationStatus;

import java.time.LocalDateTime;

public record FirmRegistrationAdminResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        String phone,
        String email,
        FirmRegistrationStatus status,
        LocalDateTime createdAt,
        LocalDateTime decidedAt,
        String decisionNote,
        String note,
        String createdBy // şimdilik boş/placeholder kullanılabilir
) {}
