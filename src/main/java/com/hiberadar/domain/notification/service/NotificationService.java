package com.hiberadar.domain.notification.service;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.notification.dto.NotificationResponse;
import com.hiberadar.domain.notification.entity.Notification;
import com.hiberadar.domain.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createMeetingConfirmedNotification(AppUser recipient, Long applicationId,
            LocalDateTime confirmedMeetingAt, String note) {
        StringBuilder msg = new StringBuilder("Basvuru #")
                .append(applicationId)
                .append(" icin randevu ")
                .append(confirmedMeetingAt)
                .append(" tarihine onaylandi.");
        if (note != null && !note.isBlank()) {
            msg.append(" Not: ").append(note);
        }
        saveNotification(recipient, "MEETING_CONFIRMED", "Randevu onaylandi", msg.toString());
    }

    public void createMeetingRequestedNotification(AppUser adminRecipient, Long applicationId, String firmUsername,
            LocalDateTime requestedAt, String note) {
        StringBuilder msg = new StringBuilder("Firma ")
                .append(firmUsername)
                .append(" basvuru #")
                .append(applicationId)
                .append(" icin ")
                .append(requestedAt)
                .append(" tarihine randevu talep etti.");
        if (note != null && !note.isBlank()) {
            msg.append(" Not: ").append(note);
        }
        saveNotification(adminRecipient, "MEETING_REQUESTED", "Yeni randevu talebi var", msg.toString());
    }

    public void createMeetingSlotCreatedNotification(AppUser firmRecipient, Long applicationId, LocalDateTime slotAt) {
        String msg = "Basvuru #" + applicationId + " icin " + slotAt + " tarihinde yeni bir randevu slotu olusturuldu.";
        saveNotification(firmRecipient, "MEETING_SLOT_CREATED", "Yeni randevu slotu olusturuldu", msg);
    }

    public void createMeetingAutoRejectedNotification(AppUser recipient,
            Long applicationId,
            LocalDateTime conflictingConfirmedAt,
            String confirmedFirmUsername) {
        StringBuilder msg = new StringBuilder("Basvuru #")
                .append(applicationId)
                .append(" icin randevu talebiniz otomatik reddedildi. ")
                .append("Ayni saatte baska bir talep onaylandi: ")
                .append(conflictingConfirmedAt)
                .append(".");
        if (confirmedFirmUsername != null && !confirmedFirmUsername.isBlank()) {
            msg.append(" Onaylanan firma: ").append(confirmedFirmUsername).append(".");
        }
        saveNotification(recipient, "MEETING_AUTO_REJECTED", "Randevu talebi otomatik reddedildi", msg.toString());
    }

    public void createMeetingRejectedNotification(AppUser recipient,
            Long applicationId,
            LocalDateTime requestedAt,
            String note) {
        StringBuilder msg = new StringBuilder("Basvuru #")
                .append(applicationId)
                .append(" icin ")
                .append(requestedAt)
                .append(" tarihli randevu talebiniz admin tarafindan reddedildi.");
        if (note != null && !note.isBlank()) {
            msg.append(" Not: ").append(note);
        }
        saveNotification(recipient, "MEETING_REJECTED", "Randevu talebi reddedildi", msg.toString());
    }

    public void createMeetingCancelledNotification(AppUser recipient,
            Long applicationId,
            LocalDateTime cancelledMeetingAt,
            String note) {
        StringBuilder msg = new StringBuilder("Basvuru #")
                .append(applicationId)
                .append(" icin ")
                .append(cancelledMeetingAt)
                .append(" tarihli onayli randevunuz admin tarafindan iptal edildi.");
        if (note != null && !note.isBlank()) {
            msg.append(" Not: ").append(note);
        }
        saveNotification(recipient, "MEETING_CANCELLED", "Onayli randevu iptal edildi", msg.toString());
    }

    public void createGrantMatchedNotification(AppUser recipient, Grant grant) {
        String message = "Profilinize uygun yeni bir hibe eklendi: #" + grant.getId() + " - " + grant.getTitle();
        boolean alreadyNotified = notificationRepository.existsByRecipient_IdAndTypeAndMessage(
                recipient.getId(),
                "GRANT_MATCHED",
                message);
        if (alreadyNotified) {
            return;
        }

        saveNotification(recipient, "GRANT_MATCHED", "Yeni uygun hibe bulundu", message);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> myNotifications(String username, Pageable pageable) {
        return notificationRepository.findByRecipient_UsernameOrderByCreatedAtDesc(username, pageable)
                .map(this::toResponse);
    }

    public long unreadCount(String username) {
        return notificationRepository.countByRecipient_UsernameAndReadFalse(username);
    }

    public NotificationResponse markAsRead(Long notificationId, String username) {
        Notification n = notificationRepository.findByIdAndRecipient_Username(notificationId, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt());
    }

    private void saveNotification(AppUser recipient, String type, String title, String message) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        notificationRepository.save(n);
    }
}
