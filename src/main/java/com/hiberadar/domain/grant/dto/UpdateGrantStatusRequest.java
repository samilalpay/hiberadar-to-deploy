package com.hiberadar.domain.grant.dto;

import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateGrantStatusRequest(
        @NotNull GrantStatus status
) {}
