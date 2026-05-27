package com.hiberadar.domain.eligibility.mapper;

import com.hiberadar.domain.eligibility.dto.EligibilityResponse;
import com.hiberadar.domain.eligibility.dto.UpsertEligibilityRequest;
import com.hiberadar.domain.eligibility.entity.EligibilityRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class EligibilityMapper {
    private EligibilityMapper() {}

    public static void apply(EligibilityRule e, UpsertEligibilityRequest req) {
        e.setApplicantTypes(toCsv(req.applicantTypes()));
        e.setMinCompanyAgeMonths(req.minCompanyAgeMonths());
        e.setMinEmployees(req.minEmployees());
        e.setMaxEmployees(req.maxEmployees());
        e.setMinTurnover(req.minTurnover());
        e.setMaxTurnover(req.maxTurnover());
        e.setTrlMin(req.trlMin());
        e.setTrlMax(req.trlMax());
        e.setRequiredCountryCodes(toCsv(req.requiredCountryCodes()));
        e.setCofundingRequired(req.cofundingRequired());
        e.setCofundingRate(req.cofundingRate());
        e.setNotes(req.notes());
    }

    public static EligibilityResponse toResponse(EligibilityRule e) {
        return new EligibilityResponse(
                e.getId(),
                e.getGrant().getId(),
                fromCsv(e.getApplicantTypes()),
                e.getMinCompanyAgeMonths(),
                e.getMinEmployees(),
                e.getMaxEmployees(),
                e.getMinTurnover(),
                e.getMaxTurnover(),
                e.getTrlMin(),
                e.getTrlMax(),
                fromCsv(e.getRequiredCountryCodes()),
                e.getCofundingRequired(),
                e.getCofundingRate(),
                e.getNotes()
        );
    }

    private static String toCsv(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return list.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(","));
    }

    private static List<String> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
