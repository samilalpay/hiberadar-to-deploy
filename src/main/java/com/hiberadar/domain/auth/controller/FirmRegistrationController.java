package com.hiberadar.domain.auth.controller;

import com.hiberadar.domain.auth.dto.RegisterFirmRequest;
import com.hiberadar.domain.auth.dto.RegisterFirmResponse;
import com.hiberadar.domain.auth.service.FirmRegistrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth/firm-registrations", "/api/firm-registrations"})
public class FirmRegistrationController {

    private final FirmRegistrationService service;

    public FirmRegistrationController(FirmRegistrationService service) {
        this.service = service;
    }

    @PostMapping
    public RegisterFirmResponse create(@Valid @RequestBody RegisterFirmRequest req) {
        return service.registerFirm(req);
    }
}
