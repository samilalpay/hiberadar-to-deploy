package com.hiberadar.domain.application.dto;

import com.hiberadar.domain.application.entity.enums.ApplicationStatus;

import java.time.LocalDateTime;

/**
 * Generic application response used by create/update and "my applications"
 * endpoints.
 */
public record ApplicationResponse(
                Long id,
                Long grantId,
                String grantTitle,
                String firmUsername,
                ApplicationStatus status,
                LocalDateTime submittedAt,
                LocalDateTime decidedAt,
                String decisionNote,
                LocalDateTime requestedMeetingAt,
                LocalDateTime confirmedMeetingAt,
                String meetingNote) {
}
