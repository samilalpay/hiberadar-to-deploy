package com.hiberadar.domain.application.service;

import com.hiberadar.domain.application.dto.ApplicationResponse;
import com.hiberadar.domain.application.dto.ConfirmMeetingRequest;
import com.hiberadar.domain.application.dto.CreateApplicationRequest;
import com.hiberadar.domain.application.dto.CreateMeetingSlotRequest;
import com.hiberadar.domain.application.dto.MeetingDecision;
import com.hiberadar.domain.application.dto.MeetingSlotResponse;
import com.hiberadar.domain.application.dto.MeetingResponse;
import com.hiberadar.domain.application.dto.MeetingCalendarItemResponse;
import com.hiberadar.domain.application.dto.RequestMeetingRequest;
import com.hiberadar.domain.application.dto.UpdateApplicationStatusRequest;
import com.hiberadar.domain.application.entity.Application;
import com.hiberadar.domain.application.entity.ApplicationMeetingSlot;
import com.hiberadar.domain.application.entity.ApplicationStatusHistory;
import com.hiberadar.domain.application.entity.enums.ApplicationStatus;
import com.hiberadar.domain.application.mapper.ApplicationMapper;
import com.hiberadar.domain.application.repository.ApplicationMeetingSlotRepository;
import com.hiberadar.domain.application.repository.ApplicationRepository;
import com.hiberadar.domain.application.repository.ApplicationStatusHistoryRepository;
import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.repository.GrantRepository;
import com.hiberadar.domain.notification.service.NotificationService;
import com.hiberadar.domain.user.entity.enums.UserRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashSet;

@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationMeetingSlotRepository meetingSlotRepository;
    private final GrantRepository grantRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationRepository applicationRepository,
            ApplicationStatusHistoryRepository historyRepository,
            ApplicationMeetingSlotRepository meetingSlotRepository,
            GrantRepository grantRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
        this.meetingSlotRepository = meetingSlotRepository;
        this.grantRepository = grantRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public ApplicationResponse create(CreateApplicationRequest req, String username) {

        AppUser firmUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Grant grant = grantRepository.findById(req.grantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found"));

        if (applicationRepository.existsByFirmUser_IdAndGrant_Id(firmUser.getId(), grant.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already applied to this grant");
        }

        Application a = new Application();
        a.setFirmUser(firmUser);
        a.setGrant(grant);
        a.setStatus(ApplicationStatus.SUBMITTED);
        a.setSubmittedAt(LocalDateTime.now());
        try {
            a = applicationRepository.save(a);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already applied to this grant");
        }

        ApplicationStatusHistory h = new ApplicationStatusHistory();
        h.setApplication(a);
        h.setFromStatus(null);
        h.setToStatus(ApplicationStatus.SUBMITTED);
        h.setNote("Application submitted");
        h.setChangedBy(firmUser);
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);

        return ApplicationMapper.toResponse(a);
    }

    public MeetingResponse requestMeeting(Long applicationId, String username, RequestMeetingRequest req) {
        Application a = applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (a.getFirmUser() == null || !a.getFirmUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot request meeting for this application");
        }

        AppUser requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        enforcePendingMeetingRequestPolicy(username);

        if (a.getRequestedMeetingAt() != null || a.getConfirmedMeetingAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Meeting is already requested/confirmed for this application");
        }
        if (req.requestedMeetingAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "requestedMeetingAt is required");
        }
        if (!isWholeHour(req.requestedMeetingAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Meeting requests must be on whole-hour boundaries (e.g. 10:00, 11:00)");
        }
        if (!req.requestedMeetingAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested meeting date must be in the future");
        }
        if (hasConfirmedMeetingConflict(req.requestedMeetingAt(), null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected time conflicts with an existing meeting");
        }

        ApplicationStatus from = a.getStatus();
        a.setRequestedMeetingAt(req.requestedMeetingAt());
        if (req.note() != null) {
            a.setMeetingNote(req.note());
        }
        a = applicationRepository.save(a);

        ApplicationStatusHistory h = new ApplicationStatusHistory();
        h.setApplication(a);
        h.setFromStatus(from);
        h.setToStatus(a.getStatus());
        h.setChangedBy(requester);
        h.setChangedAt(LocalDateTime.now());
        h.setNote("Meeting requested: " + req.requestedMeetingAt() + ((req.note() != null) ? " | " + req.note() : ""));
        historyRepository.save(h);

        List<AppUser> admins = userRepository.findByRoleIn(List.of(UserRole.ADMIN, UserRole.TEKNOPARK));
        for (AppUser admin : admins) {
            notificationService.createMeetingRequestedNotification(
                    admin,
                    a.getId(),
                    a.getFirmUser() != null ? a.getFirmUser().getUsername() : username,
                    req.requestedMeetingAt(),
                    req.note());
        }

        return new MeetingResponse(
                a.getId(),
                a.getStatus(),
                a.getRequestedMeetingAt(),
                a.getConfirmedMeetingAt(),
                a.getMeetingNote());
    }

    public MeetingResponse requestMeetingWithoutApplicationSelection(String username, RequestMeetingRequest req) {
        AppUser requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Application application = resolveOrCreateApplicationForMeeting(requester);
        return requestMeeting(application.getId(), username, req);
    }

    public MeetingResponse confirmMeeting(Long applicationId, String adminUsername, ConfirmMeetingRequest req) {
        Application a = applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        AppUser confirmer = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        MeetingDecision decision = req.decision() != null ? req.decision() : MeetingDecision.APPROVE;
        if (decision == MeetingDecision.REJECT) {
            return rejectMeetingRequest(a, confirmer, req.note());
        }

        ApplicationStatus from = a.getStatus();
        LocalDateTime targetMeetingAt = req.confirmedMeetingAt() != null
                ? req.confirmedMeetingAt()
                : a.getRequestedMeetingAt();

        if (targetMeetingAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No meeting request found to confirm");
        }
        if (!targetMeetingAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirmed meeting date must be in the future");
        }
        if (!isWholeHour(targetMeetingAt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Meeting confirmations must be on whole-hour boundaries (e.g. 10:00, 11:00)");
        }
        if (a.getRequestedMeetingAt() != null && targetMeetingAt.isBefore(a.getRequestedMeetingAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Confirmed meeting date cannot be before requested meeting date");
        }
        if (hasConfirmedMeetingConflict(targetMeetingAt, a.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected time conflicts with an existing meeting");
        }
        a.setConfirmedMeetingAt(targetMeetingAt);
        if (req.note() != null) {
            a.setMeetingNote(req.note());
        }
        a = applicationRepository.save(a);

        ApplicationStatusHistory h = new ApplicationStatusHistory();
        h.setApplication(a);
        h.setFromStatus(from);
        h.setToStatus(a.getStatus());
        h.setChangedBy(confirmer);
        h.setChangedAt(LocalDateTime.now());
        h.setNote("Meeting confirmed: " + targetMeetingAt + ((req.note() != null) ? " | " + req.note() : ""));
        historyRepository.save(h);

        if (a.getFirmUser() != null) {
            notificationService.createMeetingConfirmedNotification(
                    a.getFirmUser(),
                    a.getId(),
                    targetMeetingAt,
                    req.note());
        }

        rejectConflictingPendingRequests(targetMeetingAt, a.getId(), confirmer,
                a.getFirmUser() != null ? a.getFirmUser().getUsername() : null);

        return new MeetingResponse(
                a.getId(),
                a.getStatus(),
                a.getRequestedMeetingAt(),
                a.getConfirmedMeetingAt(),
                a.getMeetingNote());
    }

    public MeetingResponse rejectMeeting(Long applicationId, String adminUsername, String note) {
        Application a = applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        AppUser confirmer = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return rejectMeetingRequest(a, confirmer, note);
    }

    public MeetingResponse cancelMeeting(Long applicationId, String adminUsername, String note) {
        Application a = applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        AppUser canceller = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (a.getConfirmedMeetingAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No confirmed meeting found to cancel");
        }

        LocalDateTime cancelledMeetingAt = a.getConfirmedMeetingAt();
        String cancelNote = (note != null && !note.isBlank())
                ? note
                : "Randevu admin tarafindan iptal edildi.";

        a.setConfirmedMeetingAt(null);
        a.setRequestedMeetingAt(null);
        a.setMeetingNote(cancelNote);
        a.setDecisionNote(cancelNote);
        a.setDecidedAt(LocalDateTime.now());
        a = applicationRepository.save(a);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(a);
        history.setFromStatus(a.getStatus());
        history.setToStatus(a.getStatus());
        history.setChangedBy(canceller);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("Meeting cancelled by admin: " + cancelledMeetingAt + " | " + cancelNote);
        historyRepository.save(history);

        if (a.getFirmUser() != null) {
            notificationService.createMeetingCancelledNotification(
                    a.getFirmUser(),
                    a.getId(),
                    cancelledMeetingAt,
                    cancelNote);
        }

        return new MeetingResponse(
                a.getId(),
                a.getStatus(),
                a.getRequestedMeetingAt(),
                a.getConfirmedMeetingAt(),
                a.getMeetingNote());
    }

    public MeetingSlotResponse createMeetingSlot(Long applicationId, String adminUsername,
            CreateMeetingSlotRequest req) {
        Application application = applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (meetingSlotRepository.existsByApplication_IdAndSlotAt(applicationId, req.slotAt())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This slot already exists for the application");
        }
        if (hasSlotConflict(req.slotAt(), applicationId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Another slot already exists in this 30-minute window");
        }
        if (hasConfirmedMeetingConflict(req.slotAt(), null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected slot conflicts with an existing meeting");
        }
        Application applicationRef = applicationRepository.getReferenceById(applicationId);
        ApplicationMeetingSlot slot = new ApplicationMeetingSlot();
        slot.setApplication(applicationRef);
        slot.setSlotAt(req.slotAt());
        slot.setAvailable(true);
        slot = meetingSlotRepository.save(slot);

        if (application.getFirmUser() != null) {
            notificationService.createMeetingSlotCreatedNotification(application.getFirmUser(), applicationId,
                    req.slotAt());
        }

        return toSlotResponse(slot);
    }

    @Transactional(readOnly = true)
    public List<MeetingSlotResponse> availableSlotsForFirm(Long applicationId, String username) {
        Application a = applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        if (a.getFirmUser() == null || !a.getFirmUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You cannot access meeting slots for this application");
        }

        return meetingSlotRepository
                .findByApplication_IdAndAvailableTrueAndSlotAtAfterOrderBySlotAtAsc(applicationId, LocalDateTime.now())
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MeetingSlotResponse> slotsForAdmin(Long applicationId) {
        applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
        return meetingSlotRepository.findByApplication_IdOrderBySlotAtAsc(applicationId)
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MeetingSlotResponse> availableSlotsForFirmCalendar(String username) {
        return meetingSlotRepository
                .findByApplication_FirmUser_UsernameAndApplication_RequestedMeetingAtIsNullAndApplication_ConfirmedMeetingAtIsNullAndAvailableTrueAndSlotAtAfterOrderBySlotAtAsc(
                        username,
                        LocalDateTime.now())
                .stream()
                .map(this::toSlotResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MeetingCalendarItemResponse> myMeetingCalendar(String username) {
        return applicationRepository.findMeetingCalendarByFirmUsername(username)
                .stream()
                .map(this::toMeetingCalendarItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MeetingCalendarItemResponse> adminMeetingCalendar() {
        return applicationRepository.findAllMeetingCalendarItems()
                .stream()
                .map(this::toMeetingCalendarItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<java.time.LocalDate> unavailableMeetingDays() {
        return applicationRepository.findAllMeetingCalendarItems().stream()
                .filter(a -> a.getStatus() != ApplicationStatus.REJECTED)
                .map(a -> {
                    LocalDateTime effective = a.getConfirmedMeetingAt() != null ? a.getConfirmedMeetingAt()
                            : a.getRequestedMeetingAt();
                    return effective != null ? effective.toLocalDate() : null;
                })
                .filter(d -> d != null)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf));
    }

    @Transactional(readOnly = true)
    public List<LocalDateTime> occupiedMeetingTimes() {
        return applicationRepository.findAllMeetingCalendarItems().stream()
                .filter(a -> a.getStatus() != ApplicationStatus.REJECTED)
                .map(a -> a.getConfirmedMeetingAt() != null ? a.getConfirmedMeetingAt() : a.getRequestedMeetingAt())
                .filter(t -> t != null)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> myApplications(String username, Pageable pageable) {
        return applicationRepository.findByFirmUser_Username(username, pageable)
                .map(ApplicationMapper::toResponse);
    }

    /**
     * Admin/teknopark tarafı application status update.
     * changedBy kullanıcıyı SecurityContext'ten almak yerine controller principal
     * ile iletiriz.
     */
    public ApplicationResponse updateStatus(Long applicationId, UpdateApplicationStatusRequest req,
            String changedByUsername) {
        Application a = applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        AppUser changer = userRepository.findByUsername(changedByUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        ApplicationStatus from = a.getStatus();
        a.setStatus(req.status());
        if (req.status() == ApplicationStatus.APPROVED || req.status() == ApplicationStatus.REJECTED) {
            a.setDecidedAt(LocalDateTime.now());
            a.setDecisionNote(req.decisionNote());
        }
        a = applicationRepository.save(a);

        ApplicationStatusHistory h = new ApplicationStatusHistory();
        h.setApplication(a);
        h.setFromStatus(from);
        h.setToStatus(req.status());
        h.setNote(req.decisionNote());
        h.setChangedBy(changer);
        h.setChangedAt(LocalDateTime.now());
        historyRepository.save(h);

        return ApplicationMapper.toResponse(a);
    }

    private MeetingSlotResponse toSlotResponse(ApplicationMeetingSlot slot) {
        return new MeetingSlotResponse(
                slot.getId(),
                slot.getApplication().getId(),
                slot.getSlotAt(),
                slot.isAvailable());
    }

    private MeetingCalendarItemResponse toMeetingCalendarItem(Application application) {
        LocalDateTime effective = application.getConfirmedMeetingAt() != null
                ? application.getConfirmedMeetingAt()
                : application.getRequestedMeetingAt();
        String status;
        if (application.getStatus() == ApplicationStatus.REJECTED && application.getConfirmedMeetingAt() == null) {
            status = "REJECTED";
        } else if (application.getConfirmedMeetingAt() != null) {
            status = "CONFIRMED";
        } else {
            status = "REQUESTED";
        }

        return new MeetingCalendarItemResponse(
                application.getId(),
                application.getGrant() != null ? application.getGrant().getId() : null,
                application.getGrant() != null ? application.getGrant().getTitle() : null,
                application.getFirmUser() != null ? application.getFirmUser().getUsername() : null,
                application.getRequestedMeetingAt(),
                application.getConfirmedMeetingAt(),
                application.getMeetingNote(),
                effective,
                status,
                application.getSubmittedAt(),
                application.getDecidedAt(),
                application.getUpdatedAt());
    }

    private MeetingResponse rejectMeetingRequest(Application application, AppUser confirmer, String note) {
        if (application.getRequestedMeetingAt() == null || application.getConfirmedMeetingAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No pending meeting request found to reject");
        }

        ApplicationStatus from = application.getStatus();
        LocalDateTime requestedAt = application.getRequestedMeetingAt();
        String rejectNote = (note != null && !note.isBlank())
                ? note
                : "Randevu talebi admin tarafindan reddedildi.";

        application.setStatus(ApplicationStatus.REJECTED);
        application.setDecidedAt(LocalDateTime.now());
        application.setDecisionNote(rejectNote);
        application.setMeetingNote(rejectNote);
        application.setRequestedMeetingAt(null);
        application = applicationRepository.save(application);

        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(application);
        history.setFromStatus(from);
        history.setToStatus(ApplicationStatus.REJECTED);
        history.setChangedBy(confirmer);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("Meeting request rejected: " + requestedAt + " | " + rejectNote);
        historyRepository.save(history);

        if (application.getFirmUser() != null) {
            notificationService.createMeetingRejectedNotification(
                    application.getFirmUser(),
                    application.getId(),
                    requestedAt,
                    rejectNote);
        }

        return new MeetingResponse(
                application.getId(),
                application.getStatus(),
                application.getRequestedMeetingAt(),
                application.getConfirmedMeetingAt(),
                application.getMeetingNote());
    }

    private boolean hasConfirmedMeetingConflict(LocalDateTime dateTime, Long excludingApplicationId) {
        LocalDateTime start = dateTime.minusMinutes(59);
        LocalDateTime end = dateTime.plusMinutes(59);

        if (excludingApplicationId == null) {
            return applicationRepository.existsByConfirmedMeetingAtBetween(start, end);
        }

        return applicationRepository.existsByConfirmedMeetingAtBetweenAndIdNot(start, end, excludingApplicationId);
    }

    private boolean isWholeHour(LocalDateTime dateTime) {
        return dateTime.getMinute() == 0 && dateTime.getSecond() == 0 && dateTime.getNano() == 0;
    }

    private void enforcePendingMeetingRequestPolicy(String username) {
        long pendingCountFromList = applicationRepository.findWithRelationsByFirmUsername(username).stream()
                .filter(a -> a.getRequestedMeetingAt() != null && a.getConfirmedMeetingAt() == null)
                .count();
        long pendingCount = applicationRepository.countPendingMeetingRequestsByFirmUsername(username);
        long effectivePendingCount = Math.max(pendingCount, pendingCountFromList);
        if (effectivePendingCount >= 3) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "En fazla 3 onay bekleyen randevu talebiniz olabilir.");
        }
    }

    private void rejectConflictingPendingRequests(LocalDateTime confirmedMeetingAt,
            Long excludingApplicationId,
            AppUser confirmer,
            String confirmedFirmUsername) {
        LocalDateTime start = confirmedMeetingAt.minusMinutes(59);
        LocalDateTime end = confirmedMeetingAt.plusMinutes(59);
        List<Application> conflicts = applicationRepository.findPendingMeetingRequestsInWindow(start, end,
                excludingApplicationId);

        for (Application pending : conflicts) {
            ApplicationStatus previousStatus = pending.getStatus();
            String oldNote = pending.getMeetingNote();
            String autoRejectNote = "Bu saat baska bir firmaya onaylandigi icin talep otomatik reddedildi.";

            pending.setStatus(ApplicationStatus.REJECTED);
            pending.setDecidedAt(LocalDateTime.now());
            pending.setDecisionNote(autoRejectNote);
            pending.setRequestedMeetingAt(null);
            pending.setMeetingNote(
                    oldNote != null && !oldNote.isBlank() ? oldNote + " | " + autoRejectNote : autoRejectNote);
            applicationRepository.save(pending);

            ApplicationStatusHistory history = new ApplicationStatusHistory();
            history.setApplication(pending);
            history.setFromStatus(previousStatus);
            history.setToStatus(ApplicationStatus.REJECTED);
            history.setChangedBy(confirmer);
            history.setChangedAt(LocalDateTime.now());
            history.setNote("Auto rejected after another meeting confirmed at: " + confirmedMeetingAt);
            historyRepository.save(history);

            if (pending.getFirmUser() != null) {
                notificationService.createMeetingAutoRejectedNotification(
                        pending.getFirmUser(),
                        pending.getId(),
                        confirmedMeetingAt,
                        confirmedFirmUsername);
            }
        }
    }

    private boolean hasSlotConflict(LocalDateTime dateTime, Long excludingApplicationId) {
        LocalDateTime start = dateTime.minusMinutes(29);
        LocalDateTime end = dateTime.plusMinutes(29);
        if (excludingApplicationId == null) {
            return meetingSlotRepository.existsBySlotAtBetween(start, end);
        }
        return meetingSlotRepository.existsBySlotAtBetweenAndApplication_IdNot(start, end, excludingApplicationId);
    }

    private Application resolveOrCreateApplicationForMeeting(AppUser requester) {
        List<Application> existing = applicationRepository.findWithRelationsByFirmUsername(requester.getUsername());
        for (Application application : existing) {
            if (application.getRequestedMeetingAt() == null && application.getConfirmedMeetingAt() == null) {
                return application;
            }
        }

        List<Grant> published = grantRepository
                .findByStatus(com.hiberadar.domain.grant.entity.enums.GrantStatus.PUBLISHED);
        for (Grant grant : published) {
            if (!applicationRepository.existsByFirmUser_IdAndGrant_Id(requester.getId(), grant.getId())) {
                Application created = new Application();
                created.setFirmUser(requester);
                created.setGrant(grant);
                created.setStatus(ApplicationStatus.SUBMITTED);
                created.setSubmittedAt(LocalDateTime.now());
                created = applicationRepository.save(created);

                ApplicationStatusHistory history = new ApplicationStatusHistory();
                history.setApplication(created);
                history.setFromStatus(null);
                history.setToStatus(ApplicationStatus.SUBMITTED);
                history.setNote("Auto-created for meeting inquiry");
                history.setChangedBy(requester);
                history.setChangedAt(LocalDateTime.now());
                historyRepository.save(history);

                return applicationRepository.findWithRelationsById(created.getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                "Auto-created application not found"));
            }
        }

        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "No suitable published grant available to attach your meeting request");
    }
}
