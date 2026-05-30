package com.hiberadar.domain.notification.controller;

import com.hiberadar.domain.notification.dto.NotificationResponse;
import com.hiberadar.domain.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasAnyRole('FIRMA','ADMIN','TEKNOPARK')")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public Page<NotificationResponse> myNotifications(
            Authentication authentication,
            @RequestParam(required = false) Boolean read,
            Pageable pageable
    ) {
        return notificationService.myNotifications(authentication.getName(), read, pageable);
    }

    @GetMapping("/me/unread-count")
    public long unreadCount(Authentication authentication) {
        return notificationService.unreadCount(authentication.getName());
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable Long id, Authentication authentication) {
        return notificationService.markAsRead(id, authentication.getName());
    }

    @PatchMapping("/read-all")
    public int markAllAsRead(Authentication authentication) {
        return notificationService.markAllAsRead(authentication.getName());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication authentication) {
        notificationService.deleteNotification(id, authentication.getName());
    }
}
