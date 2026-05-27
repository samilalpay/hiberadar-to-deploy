package com.hiberadar.domain.application.dto;

import com.hiberadar.domain.application.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;

public record MeetingResponse(
        Long applicationId,
        ApplicationStatus status,
        LocalDateTime requestedMeetingAt,
        LocalDateTime confirmedMeetingAt,
        String meetingNote
) {}
