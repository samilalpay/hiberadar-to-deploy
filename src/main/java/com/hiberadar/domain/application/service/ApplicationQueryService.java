package com.hiberadar.domain.application.service;

import com.hiberadar.domain.application.dto.ApplicationResponse;
import com.hiberadar.domain.application.dto.ApplicationStatusHistoryResponse;
import com.hiberadar.domain.application.entity.Application;
import com.hiberadar.domain.application.entity.ApplicationStatusHistory;
import com.hiberadar.domain.application.entity.enums.ApplicationStatus;
import com.hiberadar.domain.application.mapper.ApplicationMapper;
import com.hiberadar.domain.application.repository.ApplicationRepository;
import com.hiberadar.domain.application.repository.ApplicationStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional(readOnly = true)
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;

    public ApplicationQueryService(
            ApplicationRepository applicationRepository,
            ApplicationStatusHistoryRepository historyRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.historyRepository = historyRepository;
    }

    /**
     * Admin listesi: filtrelenebilir (status, grantId, username) ve pageable.
     */
    public Page<ApplicationResponse> adminList(
            ApplicationStatus status,
            Long grantId,
            String firmUsername,
            Pageable pageable
    ) {
        return applicationRepository
                .adminList(status, grantId, firmUsername, pageable)
                .map(ApplicationMapper::toResponse);
    }

    /**
     * Admin detay: application + status history birlikte.
     */
    public ApplicationResponse adminDetail(Long applicationId) {
        Application a = applicationRepository.findWithRelationsById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Application not found"));
        return ApplicationMapper.toResponse(a);
    }

    /**
     * Admin history: sadece status değişimleri.
     */
    public List<ApplicationStatusHistoryResponse> adminHistory(Long applicationId) {
        List<ApplicationStatusHistory> history =
                historyRepository.findAllByApplication_IdOrderByChangedAtAsc(applicationId);

        return history.stream().map(h -> {
            Long changedByUserId = (h.getChangedBy() != null) ? h.getChangedBy().getId() : null;
            return new ApplicationStatusHistoryResponse(
                    h.getId(),
                    (h.getApplication() != null) ? h.getApplication().getId() : null,
                    h.getFromStatus(),
                    h.getToStatus(),
                    h.getNote(),
                    changedByUserId,
                    h.getChangedAt()
            );
        }).toList();
    }
}
