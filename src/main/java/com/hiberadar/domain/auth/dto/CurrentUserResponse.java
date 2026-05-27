package com.hiberadar.domain.auth.dto;

public record CurrentUserResponse(
        String username,
        String email,
        String role,
        boolean profileCompleted,
        boolean requiresProfileCompletion,
        String nextStep
) {
}
