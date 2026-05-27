package com.hiberadar.domain.application.entity;

import com.hiberadar.domain.application.entity.enums.ApplicationStatus;
import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.grant.entity.Grant;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_applications_firm_grant", columnNames = {"firm_user_id", "grant_id"})
        }
)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", nullable = false)
    private Grant grant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "firm_user_id", nullable = false)
    private AppUser firmUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "requested_meeting_at")
    private LocalDateTime requestedMeetingAt;

    @Column(name = "confirmed_meeting_at")
    private LocalDateTime confirmedMeetingAt;

    @Column(name = "meeting_note", length = 2000)
    private String meetingNote;

    @Column(name = "decision_note", length = 2000)
    private String decisionNote;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }

    public Grant getGrant() { return grant; }
    public void setGrant(Grant grant) { this.grant = grant; }

    public AppUser getFirmUser() { return firmUser; }
    public void setFirmUser(AppUser firmUser) { this.firmUser = firmUser; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public LocalDateTime getRequestedMeetingAt() { return requestedMeetingAt; }
    public void setRequestedMeetingAt(LocalDateTime requestedMeetingAt) { this.requestedMeetingAt = requestedMeetingAt; }

    public LocalDateTime getConfirmedMeetingAt() { return confirmedMeetingAt; }
    public void setConfirmedMeetingAt(LocalDateTime confirmedMeetingAt) { this.confirmedMeetingAt = confirmedMeetingAt; }

    public String getMeetingNote() { return meetingNote; }
    public void setMeetingNote(String meetingNote) { this.meetingNote = meetingNote; }

    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
