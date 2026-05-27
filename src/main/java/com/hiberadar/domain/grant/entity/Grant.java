package com.hiberadar.domain.grant.entity;

import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.hiberadar.domain.source.entity.Source;

@Entity
@Table(name = "grants")
public class Grant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Core info ---
    @Column(nullable = false, length = 500)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GrantStatus status;

    @Column(name = "official_url")
    private String officialUrl;

    @Column(name = "provider_name", length = 200)
    private String providerName;

    @Column(name = "program_name", length = 200)
    private String programName;

    @Column(name = "reference_code", length = 120)
    private String referenceCode;

    @Column(name = "summary_short", columnDefinition = "text")
    private String summaryShort;

    @Column(name = "admin_quick_info", columnDefinition = "text")
    private String adminQuickInfo;

    // --- Dates ---
    @Column(name = "published_at")
    private LocalDate publishedAt;

    @Column(name = "deadline_at")
    private LocalDate deadlineAt;

    // --- Money (optional) ---
    @Column(length = 3)
    private String currency;

    @Column(name = "funding_min", precision = 18, scale = 2)
    private BigDecimal fundingMin;

    @Column(name = "funding_max", precision = 18, scale = 2)
    private BigDecimal fundingMax;

    // --- Scope / geo ---
    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "nace_code", length = 20)
    private String naceCode;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InstitutionScope scope; // NATIONAL / INTERNATIONAL

    // --- Audit ---
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = GrantStatus.DRAFT;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- getters/setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public GrantStatus getStatus() { return status; }
    public void setStatus(GrantStatus status) { this.status = status; }

    public String getOfficialUrl() { return officialUrl; }
    public void setOfficialUrl(String officialUrl) { this.officialUrl = officialUrl; }

    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getReferenceCode() { return referenceCode; }
    public void setReferenceCode(String referenceCode) { this.referenceCode = referenceCode; }

    public String getSummaryShort() { return summaryShort; }
    public void setSummaryShort(String summaryShort) { this.summaryShort = summaryShort; }

    public String getAdminQuickInfo() { return adminQuickInfo; }
    public void setAdminQuickInfo(String adminQuickInfo) { this.adminQuickInfo = adminQuickInfo; }

    public LocalDate getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDate publishedAt) { this.publishedAt = publishedAt; }

    public LocalDate getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDate deadlineAt) { this.deadlineAt = deadlineAt; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getFundingMin() { return fundingMin; }
    public void setFundingMin(BigDecimal fundingMin) { this.fundingMin = fundingMin; }

    public BigDecimal getFundingMax() { return fundingMax; }
    public void setFundingMax(BigDecimal fundingMax) { this.fundingMax = fundingMax; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getNaceCode() { return naceCode; }
    public void setNaceCode(String naceCode) { this.naceCode = naceCode; }

    public InstitutionScope getScope() { return scope; }
    public void setScope(InstitutionScope scope) { this.scope = scope; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
 // --- source relation ---

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

}
