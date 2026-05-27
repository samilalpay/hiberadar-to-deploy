package com.hiberadar.domain.grant.service;

import com.hiberadar.common.config.UploadProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;

@Service
public class InstitutionLogoStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE,
        "image/webp",
        "image/svg+xml"
    );

    private final UploadProperties uploadProperties;

    public InstitutionLogoStorageService(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public String storeLogo(Long institutionId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Logo dosyası boş olamaz.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Logo dosyası PNG, JPG, WEBP veya SVG olmalıdır.");
        }

        String extension = resolveExtension(file, contentType);
        String fileName = "institution-" + institutionId + "-" + Instant.now().toEpochMilli() + extension;

        Path baseDir = Path.of(uploadProperties.getBaseDir());
        Path institutionsDir = baseDir.resolve(uploadProperties.getInstitutionsDir());
        Path target = institutionsDir.resolve(fileName).toAbsolutePath().normalize();

        try {
            Files.createDirectories(institutionsDir);
            Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Logo dosyası kaydedilemedi.", ex);
        }

        return "/uploads/" + uploadProperties.getInstitutionsDir() + "/" + fileName;
    }

    private String resolveExtension(MultipartFile file, String contentType) {
        String original = file.getOriginalFilename();
        if (StringUtils.hasText(original) && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
            if (ext.length() <= 5) {
                return ext;
            }
        }

        return switch (contentType) {
            case MediaType.IMAGE_PNG_VALUE -> ".png";
            case MediaType.IMAGE_JPEG_VALUE -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/svg+xml" -> ".svg";
            default -> "";
        };
    }
}
