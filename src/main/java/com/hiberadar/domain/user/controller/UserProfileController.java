package com.hiberadar.domain.user.controller;

import com.hiberadar.domain.user.dto.UpdateProfileRequest;
import com.hiberadar.domain.user.dto.UserProfileResponse;
import com.hiberadar.domain.user.dto.ChangePasswordRequest;
import com.hiberadar.domain.user.dto.ActionMessageResponse;
import com.hiberadar.domain.user.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("hasAnyRole('FIRMA','ADMIN','TEKNOPARK')")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return userProfileService.me(authentication.getName());
    }

    @PutMapping("/me")
    public UserProfileResponse update(@Valid @RequestBody UpdateProfileRequest req,
            Authentication authentication) {
        return userProfileService.update(authentication.getName(), req);
    }

    @PatchMapping("/me/password")
    public ActionMessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest req,
            Authentication authentication) {
        userProfileService.changePassword(authentication.getName(), req);
        return new ActionMessageResponse("Sifre basariyla guncellendi.");
    }

    @PostMapping(value = "/me/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfileResponse uploadLogo(@RequestPart("file") MultipartFile file,
            Authentication authentication) {
        return userProfileService.uploadLogo(authentication.getName(), file);
    }
}
