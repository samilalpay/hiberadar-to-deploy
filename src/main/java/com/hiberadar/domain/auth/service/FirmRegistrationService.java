package com.hiberadar.domain.auth.service;

import com.hiberadar.domain.auth.dto.*;
import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.entity.FirmRegistrationRequest;
import com.hiberadar.domain.auth.entity.enums.FirmRegistrationStatus;
import com.hiberadar.domain.auth.repository.FirmRegistrationRequestRepository;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.notification.service.NotificationService;
import com.hiberadar.domain.user.entity.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class FirmRegistrationService {

    private final FirmRegistrationRequestRepository requestRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final NotificationService notificationService;

    public FirmRegistrationService(FirmRegistrationRequestRepository requestRepo,
                                   UserRepository userRepo,
                                   PasswordEncoder encoder,
                                   NotificationService notificationService) {
        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.notificationService = notificationService;
    }

    public RegisterFirmResponse registerFirm(RegisterFirmRequest req) {
        if (userRepo.existsByUsernameIgnoreCase(req.username()) || requestRepo.existsByUsernameIgnoreCase(req.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepo.existsByEmailIgnoreCase(req.email()) || requestRepo.existsByEmailIgnoreCase(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        FirmRegistrationRequest r = new FirmRegistrationRequest();
        r.setUsername(req.username());
        r.setFirstName(req.firstName());
        r.setLastName(req.lastName());
        r.setPhone(req.phone());
        r.setEmail(req.email());
        r.setPasswordHash(encoder.encode(req.password()));
        r.setNote(req.note());
        r.setStatus(FirmRegistrationStatus.PENDING);
        requestRepo.save(r);

        var adminUsers = userRepo.findByRoleIn(java.util.List.of(UserRole.ADMIN, UserRole.TEKNOPARK));
        String contactName = String.format("%s %s", req.firstName(), req.lastName()).trim();
        for (AppUser admin : adminUsers) {
            notificationService.createFirmRegistrationRequestedNotification(
                    admin,
                    r.getUsername(),
                    contactName,
                    r.getEmail(),
                    r.getPhone());
        }

        return new RegisterFirmResponse(r.getId(), FirmRegistrationStatus.PENDING.name());
    }

    public Page<FirmRegistrationAdminResponse> listRequests(FirmRegistrationStatus status, Pageable pageable) {
        Page<FirmRegistrationRequest> page =
                (status == null) ? requestRepo.findAll(pageable) : requestRepo.findByStatus(status, pageable);

        return page.map(this::toAdminResponse);
    }

    public FirmRegistrationAdminResponse getRequest(Long id) {
        FirmRegistrationRequest r = requestRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        return toAdminResponse(r);
    }

    public FirmRegistrationAdminResponse updateStatus(Long id, UpdateFirmRegistrationStatusRequest req) {
        FirmRegistrationRequest r = requestRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        r.setStatus(req.status());
        r.setDecisionNote(req.decisionNote());
        r.setDecidedAt(LocalDateTime.now());
        requestRepo.save(r);

        // APPROVED -> app_users'a FIRMA olarak aktar
        if (req.status() == FirmRegistrationStatus.APPROVED) {
            if (userRepo.existsByUsernameIgnoreCase(r.getUsername())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists in users");
            }
            if (userRepo.existsByEmailIgnoreCase(r.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists in users");
            }

            AppUser u = new AppUser();
            u.setUsername(r.getUsername());
            u.setEmail(r.getEmail());
            u.setFirstName(r.getFirstName());
            u.setLastName(r.getLastName());
            u.setPhone(r.getPhone());
            u.setPasswordHash(r.getPasswordHash());
            u.setRole(UserRole.FIRMA);
            userRepo.save(u);
        }

        return toAdminResponse(r);
    }

    private FirmRegistrationAdminResponse toAdminResponse(FirmRegistrationRequest r) {
        return new FirmRegistrationAdminResponse(
                r.getId(),
                r.getUsername(),
            r.getFirstName(),
            r.getLastName(),
            r.getPhone(),
                r.getEmail(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getDecidedAt(),
                r.getDecisionNote(),
            r.getNote(),
                null
        );
    }
}
