package com.hiberadar.domain.application.dto;

import jakarta.validation.constraints.NotNull;

public record CreateApplicationRequest(
        @NotNull Long grantId
) {}
