package com.hiberadar.domain.grant.spec;

import com.hiberadar.domain.grant.entity.Grant;
import com.hiberadar.domain.grant.entity.enums.GrantStatus;
import com.hiberadar.domain.grant.entity.enums.InstitutionScope;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GrantSpecifications {

    private GrantSpecifications() {}

    public static Specification<Grant> hasStatus(GrantStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Grant> hasScope(InstitutionScope scope) {
        return (root, query, cb) ->
                scope == null ? cb.conjunction() : cb.equal(root.get("scope"), scope);
    }

    public static Specification<Grant> hasCountry(String countryCode) {
        return (root, query, cb) -> {
            if (countryCode == null || countryCode.isBlank()) return cb.conjunction();
            return cb.equal(cb.upper(root.get("countryCode")), countryCode.trim().toUpperCase());
        };
    }

    public static Specification<Grant> hasCurrency(String currency) {
        return (root, query, cb) -> {
            if (currency == null || currency.isBlank()) return cb.conjunction();
            String normalized = currency.trim().toUpperCase();
            return cb.equal(cb.upper(root.get("currency")), normalized);
        };
    }

    public static Specification<Grant> deadlineFrom(LocalDate from) {
        return (root, query, cb) ->
                from == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("deadlineAt"), from);
    }

    public static Specification<Grant> deadlineTo(LocalDate to) {
        return (root, query, cb) ->
                to == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("deadlineAt"), to);
    }

    public static Specification<Grant> minFunding(BigDecimal min) {
        return (root, query, cb) ->
                min == null ? cb.conjunction() : cb.greaterThanOrEqualTo(root.get("fundingMin"), min);
    }

    public static Specification<Grant> maxFunding(BigDecimal max) {
        return (root, query, cb) ->
                max == null ? cb.conjunction() : cb.lessThanOrEqualTo(root.get("fundingMax"), max);
    }

    // --- TR normalize helper: lower(unaccent(col)) ---
    private static Expression<String> trNormalizeExpr(CriteriaBuilder cb, Expression<String> expr) {
        // unaccent(text) -> text
        Expression<String> unaccented = cb.function("unaccent", String.class, expr);
        return cb.lower(unaccented);
    }

    // --- TR normalize helper for input literal ---
    private static Expression<String> trNormalizeLiteral(CriteriaBuilder cb, String value) {
        Expression<String> lit = cb.literal(value);
        Expression<String> unaccented = cb.function("unaccent", String.class, lit);
        return cb.lower(unaccented);
    }

    public static Specification<Grant> keyword(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();

            Expression<String> titleN = trNormalizeExpr(cb, root.get("title"));
            Expression<String> programN = trNormalizeExpr(cb, root.get("programName"));
            Expression<String> providerN = trNormalizeExpr(cb, root.get("providerName"));
            Expression<String> refN = trNormalizeExpr(cb, root.get("referenceCode"));

            Expression<String> qN = trNormalizeLiteral(cb, q.trim());
            // pattern = '%' || qN || '%'
            Expression<String> pattern = cb.concat(cb.concat("%", qN), "%");

            return cb.or(
                    cb.like(titleN, pattern),
                    cb.like(programN, pattern),
                    cb.like(providerN, pattern),
                    cb.like(refN, pattern)
            );
        };
    }
}
