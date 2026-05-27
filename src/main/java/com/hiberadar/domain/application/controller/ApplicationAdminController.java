package com.hiberadar.domain.application.controller;

import com.hiberadar.domain.application.dto.ApplicationResponse;
import com.hiberadar.domain.application.dto.ConfirmMeetingRequest;
import com.hiberadar.domain.application.dto.MeetingResponse;
import com.hiberadar.domain.application.dto.MeetingCalendarItemResponse;
import com.hiberadar.domain.application.dto.CancelMeetingRequest;
import com.hiberadar.domain.application.dto.RejectMeetingRequest;
import com.hiberadar.domain.application.dto.UpdateApplicationStatusRequest;
import com.hiberadar.domain.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/applications")
@PreAuthorize("hasAnyRole('ADMIN','TEKNOPARK')")
public class ApplicationAdminController {

    private final ApplicationService applicationService;

    public ApplicationAdminController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicationStatusRequest req,
            Principal principal) {
        String changer = (principal != null) ? principal.getName() : "";
        return applicationService.updateStatus(id, req, changer);
    }

    @PatchMapping("/{id}/meeting")
    public MeetingResponse confirmMeeting(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmMeetingRequest req,
            Principal principal) {
        String confirmer = (principal != null) ? principal.getName() : "";
        return applicationService.confirmMeeting(id, confirmer, req);
    }

    @PatchMapping("/{id}/meeting/reject")
    public MeetingResponse rejectMeeting(
            @PathVariable Long id,
            @RequestBody(required = false) RejectMeetingRequest req,
            Principal principal) {
        String confirmer = (principal != null) ? principal.getName() : "";
        return applicationService.rejectMeeting(id, confirmer, req != null ? req.note() : null);
    }

    @PatchMapping("/{id}/meeting/cancel")
    public MeetingResponse cancelMeeting(
            @PathVariable Long id,
            @RequestBody(required = false) CancelMeetingRequest req,
            Principal principal) {
        String confirmer = (principal != null) ? principal.getName() : "";
        return applicationService.cancelMeeting(id, confirmer, req != null ? req.note() : null);
    }

    @GetMapping("/meetings")
    public List<MeetingCalendarItemResponse> meetingCalendar() {
        return applicationService.adminMeetingCalendar();
    }

    @GetMapping("/meetings/unavailable-days")
    public List<LocalDate> unavailableMeetingDays() {
        return applicationService.unavailableMeetingDays();
    }
}
