package com.hiberadar.domain.preanalysis.entity;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.preanalysis.entity.enums.PreAnalysisStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pre_analysis_requests")
public class PreAnalysisRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "firm_user_id", nullable = false)
    private AppUser firmUser;

    @Column(name = "activity_area", nullable = false, length = 300)
    private String activityArea;

    @Column(name = "machine_park", columnDefinition = "text")
    private String machinePark;

    @Column(name = "investment_plan", columnDefinition = "text")
    private String investmentPlan;

    @Column(name = "rd_experience", columnDefinition = "text")
    private String rdExperience;

    @Column(name = "export_status", columnDefinition = "text")
    private String exportStatus;

    @Column(name = "financial_capacity", columnDefinition = "text")
    private String financialCapacity;

    @Column(name = "firm_note", columnDefinition = "text")
    private String firmNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PreAnalysisStatus status = PreAnalysisStatus.SUBMITTED;

    @Column(name = "review_note", columnDefinition = "text")
    private String reviewNote;

    @Column(name = "report_summary", columnDefinition = "text")
    private String reportSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private AppUser reviewedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public AppUser getFirmUser() { return firmUser; }
    public void setFirmUser(AppUser firmUser) { this.firmUser = firmUser; }
    public String getActivityArea() { return activityArea; }
    public void setActivityArea(String activityArea) { this.activityArea = activityArea; }
    public String getMachinePark() { return machinePark; }
    public void setMachinePark(String machinePark) { this.machinePark = machinePark; }
    public String getInvestmentPlan() { return investmentPlan; }
    public void setInvestmentPlan(String investmentPlan) { this.investmentPlan = investmentPlan; }
    public String getRdExperience() { return rdExperience; }
    public void setRdExperience(String rdExperience) { this.rdExperience = rdExperience; }
    public String getExportStatus() { return exportStatus; }
    public void setExportStatus(String exportStatus) { this.exportStatus = exportStatus; }
    public String getFinancialCapacity() { return financialCapacity; }
    public void setFinancialCapacity(String financialCapacity) { this.financialCapacity = financialCapacity; }
    public String getFirmNote() { return firmNote; }
    public void setFirmNote(String firmNote) { this.firmNote = firmNote; }
    public PreAnalysisStatus getStatus() { return status; }
    public void setStatus(PreAnalysisStatus status) { this.status = status; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public String getReportSummary() { return reportSummary; }
    public void setReportSummary(String reportSummary) { this.reportSummary = reportSummary; }
    public AppUser getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(AppUser reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
