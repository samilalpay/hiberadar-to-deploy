package com.hiberadar.domain.preanalysis.repository;

import com.hiberadar.domain.preanalysis.entity.PreAnalysisRequest;
import com.hiberadar.domain.preanalysis.entity.enums.PreAnalysisStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreAnalysisRequestRepository extends JpaRepository<PreAnalysisRequest, Long> {
    Page<PreAnalysisRequest> findByFirmUser_UsernameOrderBySubmittedAtDesc(String username, Pageable pageable);
    Page<PreAnalysisRequest> findByStatusOrderBySubmittedAtDesc(PreAnalysisStatus status, Pageable pageable);
}
