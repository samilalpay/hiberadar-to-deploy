package com.hiberadar.domain.user.service;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.user.dto.AdminFirmDetailResponse;
import com.hiberadar.domain.user.dto.AdminFirmListItem;
import com.hiberadar.domain.user.entity.enums.UserRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AdminFirmService {

    private final UserRepository userRepository;

    public AdminFirmService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminFirmListItem> list(String q, Boolean active, Pageable pageable) {
        if (q == null || q.isBlank()) {
            if (active == null) {
                return userRepository.findByRole(UserRole.FIRMA, pageable)
                        .map(this::toListItem);
            }
            return userRepository.findByRoleAndActive(UserRole.FIRMA, active, pageable)
                    .map(this::toListItem);
        }
        String keyword = q.trim();
        if (active == null) {
            return userRepository.searchByRoleAndCompanyNameOrUsername(UserRole.FIRMA, keyword, pageable)
                    .map(this::toListItem);
        }
        return userRepository.searchByRoleAndActiveAndCompanyNameOrUsername(UserRole.FIRMA, active, keyword, pageable)
                .map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public AdminFirmDetailResponse get(Long id) {
        AppUser user = getFirm(id);
        return toDetail(user);
    }

    public void delete(Long id) {
        AppUser user = getFirm(id);
        try {
            userRepository.delete(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Firma silinemedi. Iliskili kayitlar var.");
        }
    }

    public void setActive(Long id, boolean active) {
        AppUser user = getFirm(id);
        user.setActive(active);
        userRepository.save(user);
    }

    private AppUser getFirm(Long id) {
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Firma bulunamadi"));
        if (user.getRole() != UserRole.FIRMA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kullanici firma degil");
        }
        return user;
    }

    private AdminFirmListItem toListItem(AppUser user) {
        return new AdminFirmListItem(
                user.getId(),
                user.getCompanyName(),
                user.getUsername(),
                user.getEmail(),
                user.isProfileCompleted(),
                user.getSector(),
                user.getCountryCode(),
                user.getEmployees(),
            user.getCompanyLogoUrl(),
            user.isActive()
        );
    }

    private AdminFirmDetailResponse toDetail(AppUser user) {
        return new AdminFirmDetailResponse(
                user.getId(),
                user.getCompanyName(),
                user.getUsername(),
                user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhone(),
                user.getRole() == null ? null : user.getRole().name(),
                user.isProfileCompleted(),
            user.isActive(),
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
                user.getCompanyLogoUrl()
        );
    }
}
