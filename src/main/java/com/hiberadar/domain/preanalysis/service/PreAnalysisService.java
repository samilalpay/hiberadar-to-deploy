package com.hiberadar.domain.preanalysis.service;

import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.preanalysis.dto.CreatePreAnalysisRequest;
import com.hiberadar.domain.preanalysis.dto.PreAnalysisResponse;
import com.hiberadar.domain.preanalysis.dto.UpdatePreAnalysisStatusRequest;
import com.hiberadar.domain.preanalysis.entity.PreAnalysisRequest;
import com.hiberadar.domain.preanalysis.entity.enums.PreAnalysisStatus;
import com.hiberadar.domain.preanalysis.repository.PreAnalysisRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@Transactional
public class PreAnalysisService {

    private final PreAnalysisRequestRepository preAnalysisRequestRepository;
    private final UserRepository userRepository;
    private final PreAnalysisEmailService preAnalysisEmailService;

    public PreAnalysisService(PreAnalysisRequestRepository preAnalysisRequestRepository,
                              UserRepository userRepository,
                              PreAnalysisEmailService preAnalysisEmailService) {
        this.preAnalysisRequestRepository = preAnalysisRequestRepository;
        this.userRepository = userRepository;
        this.preAnalysisEmailService = preAnalysisEmailService;
    }

    public PreAnalysisResponse create(String username, CreatePreAnalysisRequest req) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        PreAnalysisRequest entity = new PreAnalysisRequest();
        entity.setFirmUser(user);
        entity.setActivityArea(req.activityArea().trim());
        entity.setMachinePark(req.machinePark());
        entity.setInvestmentPlan(req.investmentPlan());
        entity.setRdExperience(req.rdExperience());
        entity.setExportStatus(req.exportStatus());
        entity.setFinancialCapacity(req.financialCapacity());
        entity.setFirmNote(req.note());
        entity.setStatus(PreAnalysisStatus.SUBMITTED);

        return toResponse(preAnalysisRequestRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public Page<PreAnalysisResponse> myRequests(String username, Pageable pageable) {
        return preAnalysisRequestRepository.findByFirmUser_UsernameOrderBySubmittedAtDesc(username, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PreAnalysisResponse> adminList(PreAnalysisStatus status, Pageable pageable) {
        Page<PreAnalysisRequest> page = (status == null)
                ? preAnalysisRequestRepository.findAll(pageable)
                : preAnalysisRequestRepository.findByStatusOrderBySubmittedAtDesc(status, pageable);
        return page.map(this::toResponse);
    }

    public PreAnalysisResponse adminUpdate(Long id, String adminUsername, UpdatePreAnalysisStatusRequest req) {
        PreAnalysisRequest entity = preAnalysisRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pre-analysis request not found"));
        AppUser reviewer = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        entity.setStatus(req.status());
        entity.setReviewNote(req.reviewNote());
        entity.setReportSummary(req.reportSummary());
        entity.setReviewedBy(reviewer);
        entity.setReviewedAt(LocalDateTime.now());
        entity = preAnalysisRequestRepository.save(entity);

        if (req.status() == PreAnalysisStatus.COMPLETED && entity.getFirmUser() != null && entity.getFirmUser().getEmail() != null) {
            String subject = "HibeRadar - On Analiz Raporunuz Hazir";
            String body = "Merhaba " + entity.getFirmUser().getUsername() + ",\n\n"
                    + "On analiz talebiniz tamamlandi.\n\n"
                    + "Rapor Ozeti:\n"
                    + (entity.getReportSummary() == null ? "-" : entity.getReportSummary()) + "\n\n"
                    + "Not: " + (entity.getReviewNote() == null ? "-" : entity.getReviewNote());
            preAnalysisEmailService.sendReport(entity.getFirmUser().getEmail(), subject, body);
        }

        return toResponse(entity);
    }

    private PreAnalysisResponse toResponse(PreAnalysisRequest entity) {
        return new PreAnalysisResponse(
                entity.getId(),
                entity.getFirmUser() == null ? null : entity.getFirmUser().getUsername(),
                entity.getActivityArea(),
                entity.getMachinePark(),
                entity.getInvestmentPlan(),
                entity.getRdExperience(),
                entity.getExportStatus(),
                entity.getFinancialCapacity(),
                entity.getFirmNote(),
                entity.getStatus() == null ? null : entity.getStatus().name(),
                entity.getReviewNote(),
                entity.getReportSummary(),
                entity.getReviewedBy() == null ? null : entity.getReviewedBy().getUsername(),
                entity.getSubmittedAt(),
                entity.getReviewedAt()
        );
    }
}
