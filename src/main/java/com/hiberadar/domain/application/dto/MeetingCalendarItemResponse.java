package com.hiberadar.domain.application.dto;

import java.time.LocalDateTime;

public record MeetingCalendarItemResponse(
                Long applicationId,
                Long grantId,
                String grantTitle,
                String firmUsername,
                LocalDateTime requestedMeetingAt,
                LocalDateTime confirmedMeetingAt,
                String meetingNote,
                LocalDateTime effectiveMeetingAt,
                String meetingStatus,
                LocalDateTime submittedAt,
                LocalDateTime decidedAt,
                LocalDateTime updatedAt) {
}
