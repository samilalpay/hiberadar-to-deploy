package com.hiberadar.domain.application.controller;

import com.hiberadar.domain.application.dto.ApplicationResponse;
import com.hiberadar.domain.application.dto.MeetingResponse;
import com.hiberadar.domain.application.dto.MeetingCalendarItemResponse;
import com.hiberadar.domain.application.dto.CreateApplicationRequest;
import com.hiberadar.domain.application.dto.RequestMeetingRequest;
import com.hiberadar.domain.application.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/applications")
public class ApplicationCommandController {

    private final ApplicationService applicationService;

    public ApplicationCommandController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ApplicationResponse create(Authentication auth, @Valid @RequestBody CreateApplicationRequest req) {
        return applicationService.create(req, auth.getName());
    }

    @GetMapping("/me")
    public Page<ApplicationResponse> my(Authentication auth, Pageable pageable) {
        return applicationService.myApplications(auth.getName(), pageable);
    }

    @PatchMapping("/{id}/meeting")
    public MeetingResponse requestMeeting(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody RequestMeetingRequest req) {
        return applicationService.requestMeeting(id, auth.getName(), req);
    }

    @PatchMapping("/meeting")
    public MeetingResponse requestMeetingWithoutApplicationSelection(
            Authentication auth,
            @Valid @RequestBody RequestMeetingRequest req) {
        return applicationService.requestMeetingWithoutApplicationSelection(auth.getName(), req);
    }

    @GetMapping("/meetings/me")
    public List<MeetingCalendarItemResponse> myMeetingCalendar(Authentication auth) {
        return applicationService.myMeetingCalendar(auth.getName());
    }

    @GetMapping("/meetings/unavailable-days")
    public List<LocalDate> unavailableMeetingDays() {
        return applicationService.unavailableMeetingDays();
    }

    @GetMapping("/meetings/occupied-times")
    public List<LocalDateTime> occupiedMeetingTimes() {
        return applicationService.occupiedMeetingTimes();
    }
}
