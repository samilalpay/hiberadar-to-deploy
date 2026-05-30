package com.hiberadar.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterFirmRequest(
        @NotBlank @Size(min = 3, max = 120) String username,
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
                @NotBlank @Size(max = 30) @Pattern(regexp = "^5\\d{9}$", message = "Gecersiz telefon numarasi") String phone,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        String note
) {}
