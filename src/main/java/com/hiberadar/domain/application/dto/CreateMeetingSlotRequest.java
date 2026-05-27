package com.hiberadar.domain.application.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateMeetingSlotRequest(
        @NotNull @Future LocalDateTime slotAt
) {
}
