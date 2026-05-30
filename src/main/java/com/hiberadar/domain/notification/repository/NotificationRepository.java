package com.hiberadar.domain.notification.repository;

import com.hiberadar.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipient_Username(String username, Pageable pageable);
    Page<Notification> findByRecipient_UsernameAndRead(String username, boolean read, Pageable pageable);
    List<Notification> findByRecipient_UsernameAndReadFalse(String username);
    Optional<Notification> findByIdAndRecipient_Username(Long id, String username);
    long countByRecipient_UsernameAndReadFalse(String username);
    boolean existsByRecipient_IdAndTypeAndMessage(Long recipientId, String type, String message);
    void deleteByIdAndRecipient_Username(Long id, String username);
}
