package com.hiberadar.domain.auth.repository;

import com.hiberadar.domain.auth.entity.FirmRegistrationRequest;
import com.hiberadar.domain.auth.entity.enums.FirmRegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FirmRegistrationRequestRepository extends JpaRepository<FirmRegistrationRequest, Long> {

    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);

    Optional<FirmRegistrationRequest> findByEmailIgnoreCase(String email);

    Page<FirmRegistrationRequest> findByStatus(FirmRegistrationStatus status, Pageable pageable);
}
