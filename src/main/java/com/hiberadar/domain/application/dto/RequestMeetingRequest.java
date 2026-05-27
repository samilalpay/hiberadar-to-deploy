package com.hiberadar.domain.application.dto;

import java.time.LocalDateTime;

public record RequestMeetingRequest(
                LocalDateTime requestedMeetingAt,
                String note) {
}
