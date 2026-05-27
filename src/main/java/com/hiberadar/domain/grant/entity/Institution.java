package com.hiberadar.domain.grant.entity;

import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "institutions", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Institution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(nullable = false, length = 10)
    private String shortCode;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstitutionScope scope;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Constructors ---
    public Institution() {}

    public Institution(String name, String shortCode, String logoUrl, InstitutionScope scope) {
        this.name = name;
        this.shortCode = shortCode;
        this.logoUrl = logoUrl;
        this.scope = scope;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public InstitutionScope getScope() {
        return scope;
    }

    public void setScope(InstitutionScope scope) {
        this.scope = scope;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
