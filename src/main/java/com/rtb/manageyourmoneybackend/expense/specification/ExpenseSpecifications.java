package com.rtb.manageyourmoneybackend.expense.specification;

import com.rtb.manageyourmoneybackend.expense.dto.ExpenseSearchRequestDTO;
import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a {@link Specification} per filter and lets callers compose only
 * the ones that apply. Spring Data JPA's {@code Specification.and(...)}
 * treats a {@code null} operand as "no predicate", so every factory method
 * here is free to return {@code null} when its filter wasn't supplied.
 */
public final class ExpenseSpecifications {

    private ExpenseSpecifications() {
    }

    public static Specification<Expense> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Expense> hasCategoryId(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Expense> spentOnContains(String spentOn) {
        if (spentOn == null || spentOn.isBlank()) {
            return null;
        }
        String pattern = "%" + spentOn.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("spentOn")), pattern);
    }

    public static Specification<Expense> amountMatches(ExpenseSearchRequestDTO criteria) {
        if (criteria.getAmountOperator() == null || criteria.getAmount() == null) {
            return null;
        }
        return (root, query, cb) -> switch (criteria.getAmountOperator()) {
            case IS_EQUALS_TO -> cb.equal(root.get("amount"), criteria.getAmount());
            case IS_LESS_THAN -> cb.lessThan(root.get("amount"), criteria.getAmount());
            case IS_GREATER_THAN -> cb.greaterThan(root.get("amount"), criteria.getAmount());
            case IS_BETWEEN -> cb.between(root.get("amount"), criteria.getAmount(), criteria.getAmountTo());
        };
    }

    /**
     * Resolves the effective [from, to] range from either the preset or the
     * custom createdFrom/createdTo pair (preset wins when present and not ALL_TIME),
     * then builds the corresponding predicate. Either bound may be open-ended.
     */
    public static Specification<Expense> createdInRange(ExpenseSearchRequestDTO criteria) {
        Instant from = criteria.getCreatedFrom();
        Instant to = criteria.getCreatedTo();

        if (criteria.getDateRangePreset() != null && criteria.getDateRangePreset() != DateRangePreset.ALL_TIME) {
            Instant[] resolved = criteria.getDateRangePreset().resolve();
            from = resolved[0];
            to = resolved[1];
        }

        if (from == null && to == null) {
            return null;
        }

        Instant finalFrom = from;
        Instant finalTo = to;
        return (root, query, cb) -> {
            if (finalFrom != null && finalTo != null) {
                return cb.between(root.get("created"), finalFrom, finalTo);
            }
            if (finalFrom != null) {
                return cb.greaterThanOrEqualTo(root.get("created"), finalFrom);
            }
            return cb.lessThanOrEqualTo(root.get("created"), finalTo);
        };
    }

    /**
     * ANY match: true if the expense's paymentMethods (jsonb array) contains at
     * least one of the given values. Uses Postgres's built-in jsonb_exists(jsonb, text)
     * function per candidate, OR'ed together — this avoids the well-known parameter-escaping
     * headache with the jsonb "?|"/"?&" operators inside JPA/Hibernate native/JPQL strings.
     */
    public static Specification<Expense> hasAnyPaymentMethod(List<String> paymentMethods) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            Predicate[] predicates = paymentMethods.stream()
                    .map(method -> cb.isTrue(cb.function(
                            "jsonb_exists", Boolean.class, root.get("paymentMethods"), cb.literal(method))))
                    .toArray(Predicate[]::new);
            return cb.or(predicates);
        };
    }

    /**
     * Adds a LEFT JOIN FETCH on category, but only for queries whose result type is
     * the Expense entity itself. JPA disallows fetch joins on scalar/aggregate queries
     * (Spring Data's generated COUNT query, or a hand-built SUM query, would throw if this
     * fetch were added there), so this checks query.getResultType() before fetching.
     */
    public static Specification<Expense> fetchCategoryForEntityQuery() {
        return (root, query, cb) -> {
            if (Expense.class.equals(query.getResultType())) {
                root.fetch("category", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    /**
     * Combines every filter in {@code criteria} for the given user. Pass
     * {@code includeFetch = true} only for the query that actually returns
     * Expense entities (never for a count or aggregate query).
     * <p>
     * IMPORTANT: current Spring Data JPA's Specification.and()/or() call
     * {@code Assert.notNull(other, ...)} internally — a null operand throws
     * rather than being treated as a no-op (unlike some older versions). So
     * every possibly-null filter Specification is filtered out here before
     * anything is combined, rather than being passed straight into and().
     */
    public static Specification<Expense> build(Long userId, ExpenseSearchRequestDTO criteria, boolean includeFetch) {
        List<Specification<Expense>> specs = new ArrayList<>();
        specs.add(hasUserId(userId));
        addIfPresent(specs, hasCategoryId(criteria.getCategoryId()));
        addIfPresent(specs, spentOnContains(criteria.getSpentOn()));
        addIfPresent(specs, amountMatches(criteria));
        addIfPresent(specs, createdInRange(criteria));
        addIfPresent(specs, hasAnyPaymentMethod(criteria.getPaymentMethods()));
        if (includeFetch) {
            specs.add(fetchCategoryForEntityQuery());
        }

        Specification<Expense> combined = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            combined = combined.and(specs.get(i));
        }
        return combined;
    }

    private static void addIfPresent(List<Specification<Expense>> specs, Specification<Expense> spec) {
        if (spec != null) {
            specs.add(spec);
        }
    }
}