package com.hiberadar.domain.eligibility.service;

import com.hiberadar.domain.eligibility.dto.EligibilityResponse;
import com.hiberadar.domain.eligibility.dto.EligibilityCheckRequest;
import com.hiberadar.domain.eligibility.dto.EligibilityCheckResponse;
import com.hiberadar.domain.eligibility.dto.UpsertEligibilityRequest;
import com.hiberadar.domain.eligibility.entity.EligibilityRule;
import com.hiberadar.domain.eligibility.mapper.EligibilityMapper;
import com.hiberadar.domain.eligibility.repository.EligibilityRuleRepository;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.repository.GrantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EligibilityService {

    private final EligibilityRuleRepository eligibilityRuleRepository;
    private final GrantRepository grantRepository;

    public EligibilityService(EligibilityRuleRepository eligibilityRuleRepository,
            GrantRepository grantRepository) {
        this.eligibilityRuleRepository = eligibilityRuleRepository;
        this.grantRepository = grantRepository;
    }

    public EligibilityResponse upsert(Long grantId, UpsertEligibilityRequest req) {
        Grant grant = grantRepository.findById(grantId)
                .orElseThrow(() -> new IllegalArgumentException("Grant not found: " + grantId));

        EligibilityRule entity = eligibilityRuleRepository.findByGrantId(grantId)
                .orElseGet(() -> {
                    EligibilityRule e = new EligibilityRule();
                    e.setGrant(grant);
                    return e;
                });

        EligibilityMapper.apply(entity, req);

        EligibilityRule saved = eligibilityRuleRepository.save(entity);
        return EligibilityMapper.toResponse(saved);
    }

    public EligibilityResponse getByGrantId(Long grantId) {
        EligibilityRule e = eligibilityRuleRepository.findByGrantId(grantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Eligibility not found for grant: " + grantId));

        return EligibilityMapper.toResponse(e);
    }

    public EligibilityCheckResponse check(EligibilityCheckRequest req) {
        EligibilityRule rule = eligibilityRuleRepository.findByGrantId(req.grantId())
                .orElseThrow(() -> new IllegalArgumentException("Eligibility not found for grant: " + req.grantId()));

        List<String> reasons = new ArrayList<>();

        if (req.applicantType() != null && rule.getApplicantTypes() != null && !rule.getApplicantTypes().isBlank()) {
            List<String> allowedTypes = csv(rule.getApplicantTypes());
            if (!allowedTypes.contains(req.applicantType().trim().toUpperCase())) {
                reasons.add("Applicant type is not allowed");
            }
        }

        if (req.companyAgeMonths() != null && rule.getMinCompanyAgeMonths() != null
                && req.companyAgeMonths() < rule.getMinCompanyAgeMonths()) {
            reasons.add("Company age is below minimum");
        }

        if (req.employees() != null && rule.getMinEmployees() != null && req.employees() < rule.getMinEmployees()) {
            reasons.add("Employee count is below minimum");
        }
        if (req.employees() != null && rule.getMaxEmployees() != null && req.employees() > rule.getMaxEmployees()) {
            reasons.add("Employee count exceeds maximum");
        }

        if (req.turnover() != null && rule.getMinTurnover() != null
                && req.turnover().compareTo(rule.getMinTurnover()) < 0) {
            reasons.add("Turnover is below minimum");
        }
        if (req.turnover() != null && rule.getMaxTurnover() != null
                && req.turnover().compareTo(rule.getMaxTurnover()) > 0) {
            reasons.add("Turnover exceeds maximum");
        }

        if (req.countryCode() != null && rule.getRequiredCountryCodes() != null
                && !rule.getRequiredCountryCodes().isBlank()) {
            List<String> countries = csv(rule.getRequiredCountryCodes());
            if (!countries.contains(req.countryCode().trim().toUpperCase())) {
                reasons.add("Country is not eligible");
            }
        }

        if (req.cofundingRate() != null && rule.getCofundingRate() != null
                && req.cofundingRate() < rule.getCofundingRate()) {
            reasons.add("Cofunding rate is below required threshold");
        }
        if (Boolean.TRUE.equals(rule.getCofundingRequired()) && !Boolean.TRUE.equals(req.cofundingAvailable())) {
            reasons.add("Cofunding is required");
        }

        return new EligibilityCheckResponse(reasons.isEmpty(), reasons);
    }

    private List<String> csv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .toList();
    }
}
