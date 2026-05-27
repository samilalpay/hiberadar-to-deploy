package com.hiberadar.domain.application.repository;

import com.hiberadar.domain.application.entity.ApplicationStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationStatusHistoryRepository
        extends JpaRepository<ApplicationStatusHistory, Long> {

    List<ApplicationStatusHistory> findAllByApplication_IdOrderByChangedAtAsc(Long applicationId);
}
