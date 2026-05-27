package com.hiberadar.domain.application.entity;

import com.hiberadar.domain.application.entity.enums.ApplicationStatus;
import com.hiberadar.domain.auth.entity.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_status_history")
public class ApplicationStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private ApplicationStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 30)
    private ApplicationStatus toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private AppUser changedBy;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "text")
    private String note;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.changedAt == null) {
            this.changedAt = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
    }

    // getters / setters

    public Long getId() { return id; }

    public Application getApplication() { return application; }

    public ApplicationStatus getFromStatus() { return fromStatus; }

    public ApplicationStatus getToStatus() { return toStatus; }

    public AppUser getChangedBy() { return changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public String getNote() { return note; }

    public void setApplication(Application application) { this.application = application; }

    public void setFromStatus(ApplicationStatus fromStatus) { this.fromStatus = fromStatus; }

    public void setToStatus(ApplicationStatus toStatus) { this.toStatus = toStatus; }

    public void setChangedBy(AppUser changedBy) { this.changedBy = changedBy; }

    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setNote(String note) { this.note = note; }
}
