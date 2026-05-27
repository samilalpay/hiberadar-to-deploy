package com.hiberadar.domain.eligibility.entity;

import com.hiberadar.domain.eligibility.entity.enums.ApplicantType;
import com.hiberadar.domain.grant.entity.Grant;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "eligibility_rules")
public class EligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1 Grant -> 0..1 Eligibility
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grant_id", nullable = false, unique = true)
    private Grant grant;

    // Applicant types as CSV (simple + stable)
    @Column(name = "applicant_types", length = 400)
    private String applicantTypes; // e.g. "SME,STARTUP,UNIVERSITY"

    // company constraints
    @Column(name = "min_company_age_months")
    private Integer minCompanyAgeMonths;

    @Column(name = "min_employees")
    private Integer minEmployees;

    @Column(name = "max_employees")
    private Integer maxEmployees;

    @Column(name = "min_turnover", precision = 18, scale = 2)
    private BigDecimal minTurnover;

    @Column(name = "max_turnover", precision = 18, scale = 2)
    private BigDecimal maxTurnover;

    // TRL constraints
    @Column(name = "trl_min")
    private Integer trlMin;

    @Column(name = "trl_max")
    private Integer trlMax;

    // Country restrictions as CSV (simple)
    @Column(name = "required_country_codes", length = 400)
    private String requiredCountryCodes; // e.g. "TR" or "TR,DE,NL"

    // co-funding
    @Column(name = "cofunding_required")
    private Boolean cofundingRequired;

    @Column(name = "cofunding_rate")
    private Integer cofundingRate; // percent 0..100

    @Column(columnDefinition = "text")
    private String notes;

    // audit
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== getters/setters =====

    public Long getId() { return id; }

    public Grant getGrant() { return grant; }
    public void setGrant(Grant grant) { this.grant = grant; }

    public String getApplicantTypes() { return applicantTypes; }
    public void setApplicantTypes(String applicantTypes) { this.applicantTypes = applicantTypes; }

    public Integer getMinCompanyAgeMonths() { return minCompanyAgeMonths; }
    public void setMinCompanyAgeMonths(Integer minCompanyAgeMonths) { this.minCompanyAgeMonths = minCompanyAgeMonths; }

    public Integer getMinEmployees() { return minEmployees; }
    public void setMinEmployees(Integer minEmployees) { this.minEmployees = minEmployees; }

    public Integer getMaxEmployees() { return maxEmployees; }
    public void setMaxEmployees(Integer maxEmployees) { this.maxEmployees = maxEmployees; }

    public BigDecimal getMinTurnover() { return minTurnover; }
    public void setMinTurnover(BigDecimal minTurnover) { this.minTurnover = minTurnover; }

    public BigDecimal getMaxTurnover() { return maxTurnover; }
    public void setMaxTurnover(BigDecimal maxTurnover) { this.maxTurnover = maxTurnover; }

    public Integer getTrlMin() { return trlMin; }
    public void setTrlMin(Integer trlMin) { this.trlMin = trlMin; }

    public Integer getTrlMax() { return trlMax; }
    public void setTrlMax(Integer trlMax) { this.trlMax = trlMax; }

    public String getRequiredCountryCodes() { return requiredCountryCodes; }
    public void setRequiredCountryCodes(String requiredCountryCodes) { this.requiredCountryCodes = requiredCountryCodes; }

    public Boolean getCofundingRequired() { return cofundingRequired; }
    public void setCofundingRequired(Boolean cofundingRequired) { this.cofundingRequired = cofundingRequired; }

    public Integer getCofundingRate() { return cofundingRate; }
    public void setCofundingRate(Integer cofundingRate) { this.cofundingRate = cofundingRate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
