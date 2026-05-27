package com.hiberadar.domain.eligibility.repository;

import com.hiberadar.domain.eligibility.entity.EligibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EligibilityRuleRepository extends JpaRepository<EligibilityRule, Long> {
    Optional<EligibilityRule> findByGrantId(Long grantId);

    boolean existsByGrantId(Long grantId);

    void deleteByGrantId(Long grantId);
}
