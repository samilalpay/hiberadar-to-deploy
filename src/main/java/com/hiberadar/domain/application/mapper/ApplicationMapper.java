package com.hiberadar.domain.application.mapper;

import com.hiberadar.domain.application.dto.ApplicationResponse;
import com.hiberadar.domain.application.entity.Application;

public final class ApplicationMapper {
    private ApplicationMapper() {
    }

    public static ApplicationResponse toResponse(Application a) {
        if (a == null)
            return null;

        Long grantId = (a.getGrant() != null) ? a.getGrant().getId() : null;
        String grantTitle = (a.getGrant() != null) ? a.getGrant().getTitle() : null;
        String firmUsername = (a.getFirmUser() != null) ? a.getFirmUser().getUsername() : null;

        return new ApplicationResponse(
                a.getId(),
                grantId,
                grantTitle,
                firmUsername,
                a.getStatus(),
                a.getSubmittedAt(),
                a.getDecidedAt(),
                a.getDecisionNote(),
                a.getRequestedMeetingAt(),
                a.getConfirmedMeetingAt(),
                a.getMeetingNote());
    }
}
