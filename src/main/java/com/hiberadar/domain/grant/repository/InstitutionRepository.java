package com.hiberadar.domain.grant.repository;

import com.hiberadar.domain.grant.entity.Institution;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByNameIgnoreCase(String name);

    List<Institution> findByScope(InstitutionScope scope);

    List<Institution> findAllByOrderByName();
}
