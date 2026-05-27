package com.hiberadar.domain.grant.service;

import com.hiberadar.domain.grant.dto.GrantDetailDto;
import com.hiberadar.domain.grant.dto.GrantListItemDto;
import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import com.hiberadar.domain.grant.mapper.GrantMapper;
import com.hiberadar.domain.grant.repository.GrantRepository;
import com.hiberadar.domain.auth.entity.AppUser;
import com.hiberadar.domain.auth.repository.UserRepository;
import com.hiberadar.domain.eligibility.entity.EligibilityRule;
import com.hiberadar.domain.eligibility.repository.EligibilityRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class GrantQueryService {

    private static final Pattern NACE_PATTERN = Pattern.compile("^\\d{2}(?:\\.\\d{2}){0,3}$");

    private final GrantRepository grantRepository;
    private final UserRepository userRepository;
    private final EligibilityRuleRepository eligibilityRuleRepository;

    public GrantQueryService(GrantRepository grantRepository,
            UserRepository userRepository,
            EligibilityRuleRepository eligibilityRuleRepository) {
        this.grantRepository = grantRepository;
        this.userRepository = userRepository;
        this.eligibilityRuleRepository = eligibilityRuleRepository;
    }

    public Page<GrantListItemDto> list(
            GrantStatus status,
            Long sourceId,
            String nace,
            InstitutionScope scope,
            String countryCode,
            String currency,
            String q,
            LocalDate deadlineFrom,
            LocalDate deadlineTo,
            BigDecimal minFunding,
            BigDecimal maxFunding,
            Pageable pageable) {
        String statusStr = (status == null) ? null : status.name();
        String scopeStr = (scope == null) ? null : scope.name();

        // PRD: default only active/open calls unless explicitly requested otherwise.
        if (statusStr == null) {
            statusStr = GrantStatus.PUBLISHED.name();
        }

        return grantRepository.search(
                statusStr,
                sourceId,
                nace,
                scopeStr,
                countryCode,
                currency,
                q,
                deadlineFrom,
                deadlineTo,
                minFunding,
                maxFunding,
                pageable).map(GrantMapper::toListItem);
    }

    public GrantDetailDto detail(Long id) {
        Grant g = grantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grant not found"));
        return GrantMapper.toDetail(g);
    }

    public Page<GrantListItemDto> matchedForUser(String username, Pageable pageable) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!user.isProfileCompleted()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please complete your profile first");
        }

        List<GrantListItemDto> allMatches = grantRepository.findByStatus(GrantStatus.PUBLISHED).stream()
                .map(grant -> toScoredMatch(grant, user))
                .filter(dto -> dto != null)
                .sorted((a, b) -> Integer.compare(
                        b.matchScore == null ? 0 : b.matchScore,
                        a.matchScore == null ? 0 : a.matchScore))
                .toList();

        int from = (int) pageable.getOffset();
        if (from >= allMatches.size()) {
            return new PageImpl<>(List.of(), pageable, allMatches.size());
        }
        int to = Math.min(from + pageable.getPageSize(), allMatches.size());
        return new PageImpl<>(allMatches.subList(from, to), pageable, allMatches.size());
    }

    private boolean isEligibleForUser(Grant grant, AppUser user) {
        EligibilityRule rule = eligibilityRuleRepository.findByGrantId(grant.getId()).orElse(null);
        if (rule == null) {
            return false;
        }

        if (!matchesProfileDimensions(grant, user)) {
            return false;
        }

        if (rule.getApplicantTypes() != null && !rule.getApplicantTypes().isBlank()) {
            if (user.getApplicantType() == null
                    || !csv(rule.getApplicantTypes()).contains(user.getApplicantType().trim().toUpperCase())) {
                return false;
            }
        }
        if (rule.getMinCompanyAgeMonths() != null
                && (user.getCompanyAgeMonths() == null || user.getCompanyAgeMonths() < rule.getMinCompanyAgeMonths())) {
            return false;
        }
        if (rule.getMinEmployees() != null
                && (user.getEmployees() == null || user.getEmployees() < rule.getMinEmployees())) {
            return false;
        }
        if (rule.getMaxEmployees() != null
                && (user.getEmployees() == null || user.getEmployees() > rule.getMaxEmployees())) {
            return false;
        }
        if (rule.getMinTurnover() != null
                && (user.getTurnover() == null || user.getTurnover().compareTo(rule.getMinTurnover()) < 0)) {
            return false;
        }
        if (rule.getMaxTurnover() != null
                && (user.getTurnover() == null || user.getTurnover().compareTo(rule.getMaxTurnover()) > 0)) {
            return false;
        }
        if (rule.getRequiredCountryCodes() != null && !rule.getRequiredCountryCodes().isBlank()) {
            if (user.getCountryCode() == null
                    || !csv(rule.getRequiredCountryCodes()).contains(user.getCountryCode().trim().toUpperCase())) {
                return false;
            }
        }
        if (Boolean.TRUE.equals(rule.getCofundingRequired()) && !Boolean.TRUE.equals(user.getCofundingAvailable())) {
            return false;
        }
        if (rule.getCofundingRate() != null
                && (user.getCofundingRate() == null || user.getCofundingRate() < rule.getCofundingRate())) {
            return false;
        }

        return true;
    }

    private GrantListItemDto toScoredMatch(Grant grant, AppUser user) {
        if (!isEligibleForUser(grant, user)) {
            return null;
        }

        ScoreResult score = calculateScore(grant, user);
        if (score.score() <= 0) {
            return null;
        }

        GrantListItemDto dto = GrantMapper.toListItem(grant);
        dto.matchScore = score.score();
        dto.matchReasons = score.reasons();
        return dto;
    }

    public boolean isMatchedForUser(Grant grant, AppUser user) {
        if (grant == null || user == null) {
            return false;
        }
        if (grant.getStatus() != GrantStatus.PUBLISHED) {
            return false;
        }
        if (!isEligibleForUser(grant, user)) {
            return false;
        }
        return calculateScore(grant, user).score() > 0;
    }

    private boolean matchesProfileDimensions(Grant grant, AppUser user) {
        if (user.getNaceCodes() != null && !user.getNaceCodes().isBlank() && grant.getNaceCode() != null
                && !grant.getNaceCode().isBlank()) {
            if (!matchesNace(user.getNaceCodes(), grant.getNaceCode())) {
                return false;
            }
        }

        String searchableGrantText = normalizeForMatch(
                safe(grant.getTitle()) + " " + safe(grant.getProgramName()) + " " + safe(grant.getProviderName()));
        if (user.getSector() != null && !user.getSector().isBlank()) {
            if (!searchableGrantText.contains(normalizeForMatch(user.getSector()))) {
                return false;
            }
        }
        if (user.getActivityArea() != null && !user.getActivityArea().isBlank()) {
            if (!searchableGrantText.contains(normalizeForMatch(user.getActivityArea()))) {
                return false;
            }
        }

        if (user.getTurnover() != null && grant.getFundingMin() != null
                && user.getTurnover().compareTo(grant.getFundingMin()) < 0) {
            return false;
        }
        return true;
    }

    private ScoreResult calculateScore(Grant grant, AppUser user) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (user.getNaceCodes() != null && !user.getNaceCodes().isBlank() && grant.getNaceCode() != null
                && !grant.getNaceCode().isBlank()
                && matchesNace(user.getNaceCodes(), grant.getNaceCode())) {
            score += 35;
            reasons.add("NACE uyumu");
        }

        String searchableGrantText = normalizeForMatch(
                safe(grant.getTitle()) + " " + safe(grant.getProgramName()) + " " + safe(grant.getProviderName()));
        if (user.getSector() != null && !user.getSector().isBlank()
                && searchableGrantText.contains(normalizeForMatch(user.getSector()))) {
            score += 20;
            reasons.add("Sektor uyumu");
        }
        if (user.getActivityArea() != null && !user.getActivityArea().isBlank()
                && searchableGrantText.contains(normalizeForMatch(user.getActivityArea()))) {
            score += 20;
            reasons.add("Faaliyet alani uyumu");
        }
        if (user.getCountryCode() != null && grant.getCountryCode() != null
                && user.getCountryCode().equalsIgnoreCase(grant.getCountryCode())) {
            score += 10;
            reasons.add("Ulke uyumu");
        }
        if (user.getTurnover() != null) {
            if (grant.getFundingMax() != null && grant.getFundingMax().compareTo(user.getTurnover()) <= 0) {
                score += 10;
                reasons.add("Butce uyumu");
            } else if (grant.getFundingMin() == null || grant.getFundingMin().compareTo(user.getTurnover()) <= 0) {
                score += 5;
                reasons.add("Kismi butce uyumu");
            }
        }
        if (grant.getDeadlineAt() != null) {
            LocalDate now = LocalDate.now();
            if (!grant.getDeadlineAt().isBefore(now.plusDays(30))) {
                score += 5;
                reasons.add("Uygun son basvuru tarihi");
            }
        }

        return new ScoreResult(Math.min(score, 100), reasons);
    }

    private List<String> csv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toUpperCase)
                .toList();
    }

    private boolean matchesNace(String userNaceCodes, String grantNaceCode) {
        String grant = normalizeNaceCode(grantNaceCode);
        if (grant.isBlank()) {
            return false;
        }

        return userNaceCodes(userNaceCodes).stream().anyMatch(code -> grant.equals(code)
                || grant.startsWith(code + ".")
                || code.startsWith(grant + "."));
    }

    private List<String> userNaceCodes(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("[,;\\n]+"))
                .map(this::normalizeNaceCode)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String normalizeNaceCode(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        if (!NACE_PATTERN.matcher(normalized).matches()) {
            return "";
        }
        return normalized;
    }

    private String normalizeForMatch(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.forLanguageTag("tr-TR"));
        String folded = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return folded.replace('ı', 'i');
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private record ScoreResult(int score, List<String> reasons) {
    }
}
