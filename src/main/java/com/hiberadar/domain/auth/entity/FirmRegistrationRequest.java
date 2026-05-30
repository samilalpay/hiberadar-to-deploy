package com.hiberadar.domain.auth.entity;

import com.hiberadar.domain.auth.entity.enums.FirmRegistrationStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "firm_registration_requests",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_firm_reg_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_firm_reg_email", columnNames = "email")
        }
)
public class FirmRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String username;

        @Column(name = "first_name", nullable = false, length = 80)
        private String firstName;

        @Column(name = "last_name", nullable = false, length = 80)
        private String lastName;

        @Column(name = "phone", nullable = false, length = 30)
        private String phone;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FirmRegistrationStatus status = FirmRegistrationStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "decision_note", length = 400)
    private String decisionNote;

        @Column(name = "note", length = 500)
        private String note;

    // getters/setters
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public FirmRegistrationStatus getStatus() { return status; }
    public void setStatus(FirmRegistrationStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }

    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
}
