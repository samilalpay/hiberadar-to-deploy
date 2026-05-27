package com.hiberadar.domain.auth.dto;

public record RegisterFirmResponse(
        Long requestId,
        String status // "PENDING"
) {}
