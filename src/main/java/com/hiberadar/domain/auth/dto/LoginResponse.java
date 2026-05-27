package com.hiberadar.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        String username,
        String role,
        boolean profileCompleted,
        boolean requiresProfileCompletion,
        String nextStep
) {}
