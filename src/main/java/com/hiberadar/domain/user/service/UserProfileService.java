package com.hiberadar.domain.user.service;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.user.dto.ChangePasswordRequest;
import com.hiberadar.domain.user.dto.UpdateProfileRequest;
import com.hiberadar.domain.user.dto.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Service
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirmLogoStorageService firmLogoStorageService;

    public UserProfileService(UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              FirmLogoStorageService firmLogoStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.firmLogoStorageService = firmLogoStorageService;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse me(String username) {
        AppUser user = getUser(username);
        return toResponse(user);
    }

    public UserProfileResponse update(String username, UpdateProfileRequest req) {
        AppUser user = getUser(username);

        user.setCompanyName(req.companyName().trim());
        user.setApplicantType(req.applicantType().trim().toUpperCase());
        user.setCompanyAgeMonths(req.companyAgeMonths());
        user.setEmployees(req.employees());
        user.setCountryCode(req.countryCode().trim().toUpperCase());
        user.setCofundingAvailable(req.cofundingAvailable());
        user.setCofundingRate(req.cofundingRate());
        user.setSector(req.sector().trim());
        user.setActivityArea(req.activityArea().trim());
        user.setTurnover(req.turnover());
        user.setNaceCodes(normalizeNaceCodes(req.naceCodes()));
        user.setProfileCompleted(isProfileComplete(user));

        user = userRepository.save(user);
        return toResponse(user);
    }

    public void changePassword(String username, ChangePasswordRequest req) {
        AppUser user = getUser(username);

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mevcut sifre hatali");
        }

        if (req.newPassword().trim().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yeni sifre en az 8 karakter olmali");
        }

        if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Yeni sifre mevcut sifre ile ayni olamaz");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    public UserProfileResponse uploadLogo(String username, org.springframework.web.multipart.MultipartFile file) {
        AppUser user = getUser(username);

        String logoUrl = firmLogoStorageService.storeLogo(user.getId(), file);
        user.setCompanyLogoUrl(logoUrl);
        userRepository.save(user);

        return toResponse(user);
    }

    private AppUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private boolean isProfileComplete(AppUser user) {
        return user.getApplicantType() != null
            && user.getCompanyName() != null
            && !user.getCompanyName().isBlank()
                && !user.getApplicantType().isBlank()
                && user.getCompanyAgeMonths() != null
                && user.getEmployees() != null
                && user.getCountryCode() != null
                && !user.getCountryCode().isBlank()
                && user.getCofundingAvailable() != null
                && user.getCofundingRate() != null
                && user.getSector() != null
                && !user.getSector().isBlank()
                && user.getActivityArea() != null
                && !user.getActivityArea().isBlank()
                && user.getTurnover() != null
                && user.getNaceCodes() != null
                && !user.getNaceCodes().isBlank();
    }

    private UserProfileResponse toResponse(AppUser user) {
        return new UserProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getRole() == null ? null : user.getRole().name(),
                user.isProfileCompleted(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
                user.getCompanyName(),
                user.getApplicantType(),
                user.getCompanyAgeMonths(),
                user.getEmployees(),
                user.getCountryCode(),
                user.getCofundingAvailable(),
                user.getCofundingRate(),
                user.getSector(),
                user.getActivityArea(),
                user.getTurnover(),
                user.getNaceCodes(),
                user.getCompanyLogoUrl());
    }

    private String normalizeNaceCodes(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Arrays.stream(value.split("[,;\\n]+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
