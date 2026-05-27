package com.hiberadar.domain.application.dto;

import java.time.LocalDateTime;

public record MeetingSlotResponse(
        Long id,
        Long applicationId,
        LocalDateTime slotAt,
        boolean available
) {
}
