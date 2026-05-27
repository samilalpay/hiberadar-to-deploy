package com.hiberadar.domain.application.repository;

import com.hiberadar.domain.application.entity.ApplicationMeetingSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ApplicationMeetingSlotRepository extends JpaRepository<ApplicationMeetingSlot, Long> {
    Optional<ApplicationMeetingSlot> findByIdAndApplication_Id(Long id, Long applicationId);

    boolean existsByApplication_IdAndSlotAt(Long applicationId, LocalDateTime slotAt);

    boolean existsBySlotAt(LocalDateTime slotAt);

    boolean existsBySlotAtAndApplication_IdNot(LocalDateTime slotAt, Long applicationId);

    boolean existsBySlotAtBetween(LocalDateTime start, LocalDateTime end);

    boolean existsBySlotAtBetweenAndApplication_IdNot(LocalDateTime start, LocalDateTime end, Long applicationId);

    List<ApplicationMeetingSlot> findByApplication_IdAndAvailableTrueAndSlotAtAfterOrderBySlotAtAsc(Long applicationId,
            LocalDateTime now);

    List<ApplicationMeetingSlot> findByApplication_IdOrderBySlotAtAsc(Long applicationId);

    List<ApplicationMeetingSlot> findByApplication_FirmUser_UsernameAndApplication_RequestedMeetingAtIsNullAndApplication_ConfirmedMeetingAtIsNullAndAvailableTrueAndSlotAtAfterOrderBySlotAtAsc(
            String username,
            LocalDateTime now);
}
