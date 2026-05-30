package com.hiberadar.domain.user.service;

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
public class FirmLogoStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            "image/svg+xml");

    private final UploadProperties uploadProperties;

    public FirmLogoStorageService(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public String storeLogo(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Logo dosyasi bos olamaz.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Logo dosyasi PNG, JPG, WEBP veya SVG olmali.");
        }

        String extension = resolveExtension(file, contentType);
        String fileName = "firm-" + userId + "-" + Instant.now().toEpochMilli() + extension;

        Path baseDir = Path.of(uploadProperties.getBaseDir());
        Path firmDir = baseDir.resolve(uploadProperties.getFirmLogosDir());
        Path target = firmDir.resolve(fileName).toAbsolutePath().normalize();

        try {
            Files.createDirectories(firmDir);
            Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Logo dosyasi kaydedilemedi.", ex);
        }

        return "/uploads/" + uploadProperties.getFirmLogosDir() + "/" + fileName;
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
