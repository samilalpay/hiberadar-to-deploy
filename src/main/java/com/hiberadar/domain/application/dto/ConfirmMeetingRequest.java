package com.hiberadar.domain.application.dto;

import jakarta.validation.constraints.Future;

import java.time.LocalDateTime;

public record ConfirmMeetingRequest(
                @Future LocalDateTime confirmedMeetingAt,
                String note,
                MeetingDecision decision) {
}
