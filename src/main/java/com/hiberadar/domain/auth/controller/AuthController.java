package com.hiberadar.domain.auth.controller;

import com.hiberadar.common.security.JwtService;
import com.hiberadar.domain.auth.dto.CurrentUserResponse;
import com.hiberadar.domain.auth.dto.LoginRequest;
import com.hiberadar.domain.auth.dto.LoginResponse;
import com.hiberadar.domain.auth.dto.RegisterFirmRequest;
import com.hiberadar.domain.auth.dto.RegisterFirmResponse;
import com.hiberadar.domain.auth.dto.RegisterRequest;
import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.auth.service.FirmRegistrationService;
import com.hiberadar.domain.user.entity.enums.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FirmRegistrationService firmRegistrationService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          FirmRegistrationService firmRegistrationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.firmRegistrationService = firmRegistrationService;
    }

    // ✅ Firma self-register -> request tablosuna düşer (admin approve edince app_users)
    @PostMapping("/register-firm")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterFirmResponse registerFirm(@Valid @RequestBody RegisterFirmRequest req) {
        return firmRegistrationService.registerFirm(req);
    }

    // (Eski) direkt register - istersen dev amaçlı bırakıyoruz
    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest req) {

        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.findByEmailIgnoreCase(req.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        AppUser u = new AppUser();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(UserRole.FIRMA);

        userRepository.save(u);

        String token = jwtService.generateToken(u.getUsername(), u.getRole().name());
        return buildLoginResponse(u, token);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {

        AppUser u = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!u.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hesap pasif durumda");
        }

        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String role = (u.getRole() == null) ? UserRole.FIRMA.name() : u.getRole().name();
        String token = jwtService.generateToken(u.getUsername(), role);

        return buildLoginResponse(u, token);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(Authentication authentication) {
        AppUser user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return buildCurrentUserResponse(user);
    }

    private LoginResponse buildLoginResponse(AppUser user, String token) {
        CurrentUserResponse currentUser = buildCurrentUserResponse(user);
        return new LoginResponse(
                token,
                "Bearer",
                currentUser.username(),
                currentUser.role(),
                currentUser.profileCompleted(),
                currentUser.requiresProfileCompletion(),
                currentUser.nextStep()
        );
    }

    private CurrentUserResponse buildCurrentUserResponse(AppUser user) {
        String role = (user.getRole() == null) ? UserRole.FIRMA.name() : user.getRole().name();
        boolean requiresProfile = user.getRole() == UserRole.FIRMA && !user.isProfileCompleted();
        return new CurrentUserResponse(
                user.getUsername(),
                user.getEmail(),
                role,
                user.isProfileCompleted(),
                requiresProfile,
                requiresProfile ? "/profile" : "/dashboard"
        );
    }
}
