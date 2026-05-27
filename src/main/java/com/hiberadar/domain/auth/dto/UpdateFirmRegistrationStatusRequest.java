package com.hiberadar.domain.auth.dto;

import com.hiberadar.domain.auth.entity.enums.FirmRegistrationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateFirmRegistrationStatusRequest(
        @NotNull FirmRegistrationStatus status,
        String decisionNote
) {}
