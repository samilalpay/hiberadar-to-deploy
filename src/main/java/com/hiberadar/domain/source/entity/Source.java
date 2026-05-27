package com.hiberadar.domain.source.entity;

import com.hiberadar.domain.source.entity.enums.SourceCategory;
import com.hiberadar.domain.source.entity.enums.SourceScope;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "sources")
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 250)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SourceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SourceScope scope;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "official_url", length = 500)
    private String officialUrl;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public SourceCategory getCategory() { return category; }
    public SourceScope getScope() { return scope; }
    public String getCountryCode() { return countryCode; }
    public String getOfficialUrl() { return officialUrl; }
    public String getNotes() { return notes; }
    public boolean isActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setCategory(SourceCategory category) { this.category = category; }
    public void setScope(SourceScope scope) { this.scope = scope; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setActive(boolean active) { this.active = active; }
}
