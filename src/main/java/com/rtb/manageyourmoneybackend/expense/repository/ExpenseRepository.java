package com.rtb.manageyourmoneybackend.expense.repository;

import com.rtb.manageyourmoneybackend.expense.dto.ExpenseResponseDTO;
import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryExpenseSummary;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

    @QueryHints(value = {
            @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("SELECT e FROM Expense e JOIN FETCH e.category WHERE e.user.id = :userId")
    Stream<Expense> streamByUserId(Long userId);

    @Query("""
            SELECT CONCAT(e.category.id, ':', e.amount, ':', COALESCE(e.spentOn, ''))\s
            FROM Expense e\s
            WHERE e.user.id = :userId\s
            AND e.category.id IN :categoryIds
            """)
    Set<String> findExistingFingerprints(
            @Param("userId") Long userId,
            @Param("categoryIds") Set<Long> categoryIds
    );


    /**
     * Plain entity fetch for the mutation path (update/delete). No category
     * data beyond its id/proxy is touched there, so no extra fetching is
     * needed here — see the projection queries below for the read paths.
     */
    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * DTO projection: selects only c.id and c.name off the category (not the
     * whole ExpenseCategory entity), and builds the response DTO directly in
     * the query. One round trip, no lazy-loading, no unused columns.
     */
    @Query("""
            select new com.rtb.manageyourmoneybackend.expense.dto.ExpenseResponseDTO(
                e.id, e.spentOn, e.amount, c.id, c.name, e.paymentMethods,
                e.isSynced, e.user.id, e.created, e.modified)
            from Expense e join e.category c
            where e.id = :id and e.user.id = :userId
            """)
    Optional<ExpenseResponseDTO> findResponseByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query(value = """
            select new com.rtb.manageyourmoneybackend.expense.dto.ExpenseResponseDTO(
                e.id, e.spentOn, e.amount, c.id, c.name, e.paymentMethods,
                e.isSynced, e.user.id, e.created, e.modified)
            from Expense e join e.category c
            where e.user.id = :userId order by e.modified desc
            """,
            countQuery = "select count(e) from Expense e where e.user.id = :userId")
    Page<ExpenseResponseDTO> findAllResponsesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query(value = """
            select new com.rtb.manageyourmoneybackend.expense.dto.ExpenseResponseDTO(
                e.id, e.spentOn, e.amount, c.id, c.name, e.paymentMethods,
                e.isSynced, e.user.id, e.created, e.modified)
            from Expense e join e.category c
            where e.user.id = :userId and e.category.id = :categoryId order by e.modified desc
            """,
            countQuery = "select count(e) from Expense e where e.user.id = :userId and e.category.id = :categoryId")
    Page<ExpenseResponseDTO> findAllResponsesByUserIdAndCategoryId(
            @Param("userId") Long userId, @Param("categoryId") Long categoryId, Pageable pageable);


    /**
     * Returns each expense's paymentMethods list (jsonb column) for the given user,
     * including nulls/empties as-is. The service layer flattens and de-duplicates
     * these, since a portable JPQL "unnest" over a jsonb array isn't available here.
     */
    @Query("select e.paymentMethods from Expense e where e.user.id = :userId")
    List<List<String>> findPaymentMethodsByUserId(@Param("userId") Long userId);


    /**
     * COALESCE guards against SUM returning null when the user has no expenses at all,
     * so callers always get a usable BigDecimal (zero) instead of null.
     */
    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.user.id = :userId")
    BigDecimal sumAmountByUserId(@Param("userId") Long userId);

    @Query("""
            select coalesce(sum(e.amount), 0) from Expense e
            where e.user.id = :userId and e.category.id = :categoryId
            """)
    BigDecimal sumAmountByUserIdAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query("SELECT new com.rtb.manageyourmoneybackend.expense.dto.CategoryExpenseSummary(" +
            "ex.category.id, SUM(ex.amount)) " +
            "FROM Expense ex " +
            "WHERE ex.user.id = :userId " +
            "GROUP BY ex.category.id")
    List<CategoryExpenseSummary> sumAmountByUserIdGroupedByCategory(@Param("userId") Long userId);

    /**
     * Grouped monthly totals for one calendar year, filtered by a [yearStart, yearEnd)
     * range rather than date_part('year', created) = :year: a range comparison on
     * "created" can still use idx_expenses_user_created, whereas wrapping the column
     * in date_part(...) would force a full scan since a plain B-tree index can't be
     * used through a function call without a matching expression index.
     * <p>
     * categoryId/paymentMethodsCsv are nullable — pass null to mean "no filter".
     * paymentMethodsCsv is a comma-separated list, matched via jsonb_exists (see
     * ExpenseSpecifications#hasAnyPaymentMethod for why "??|" is escaped this way
     * rather than using the operator directly).
     * <p>
     * Returns one row per month that has at least one matching expense — months with
     * zero expenses are simply absent, and are zero-filled by the service layer.
     */
    @Query(value = """
            SELECT date_part('month', e.created)::int AS month, COALESCE(SUM(e.amount), 0) AS total
            FROM expenses e
            WHERE e.user_id = :userId
              AND e.created >= :yearStart AND e.created < :yearEnd
              AND (:categoryId IS NULL OR e.category_id = :categoryId)
              AND (:paymentMethodsCsv IS NULL OR EXISTS (
                  SELECT 1 FROM jsonb_array_elements_text(e.payment_methods) AS pm(value)
                  WHERE pm.value = ANY(string_to_array(:paymentMethodsCsv, ','))
              ))
            GROUP BY month
            ORDER BY month
            """, nativeQuery = true)
    List<Object[]> findMonthlyTotals(
            @Param("userId") Long userId,
            @Param("yearStart") Instant yearStart,
            @Param("yearEnd") Instant yearEnd,
            @Param("categoryId") Long categoryId,
            @Param("paymentMethodsCsv") String paymentMethodsCsv);

    /**
     * Category-wise totals over an optional [from, to] range (either/both null = unbounded).
     * NOTE: assumes the categories table is named "expense_categories" with "id"/"name"
     * columns — adjust if your actual table name differs (@Table on ExpenseCategory
     * wasn't visible when this was written).
     * Returns one row per category that has at least one matching expense; categories
     * with zero expenses in range are simply absent (there's no "all categories" list
     * to zero-fill against here, unlike the fixed 12-month/7-day cases).
     */
    @Query(value = """
        SELECT c.id, c.name, COALESCE(SUM(e.amount), 0) AS total
        FROM expenses e
        JOIN expense_categories c ON c.id = e.category_id
        WHERE e.user_id = :userId
          AND (CAST(:createdFrom AS timestamptz) IS NULL OR e.created >= :createdFrom)
          AND (CAST(:createdTo AS timestamptz) IS NULL OR e.created <= :createdTo)
        GROUP BY c.id, c.name
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> findCategoryBreakdown(
            @Param("userId") Long userId,
            @Param("createdFrom") Instant createdFrom,
            @Param("createdTo") Instant createdTo);

    /**
     * Per-payment-method totals over an optional [from, to] range. Unnests the jsonb
     * payment_methods array per expense via a LATERAL join: an expense with no payment
     * methods (null or empty array) simply contributes no rows, so it's naturally excluded
     * rather than needing a null-guard. An expense listing multiple methods contributes
     * its full amount once per method listed — see PaymentMethodDistributionResponseDTO's
     * javadoc for why that's intentional, not double-counting.
     */
    @Query(value = """
              SELECT pm.value AS payment_method, COALESCE(SUM(e.amount), 0) AS total
              FROM expenses e
              CROSS JOIN LATERAL jsonb_array_elements_text(e.payment_methods) AS pm(value)
              WHERE e.user_id = :userId
                AND (CAST(:createdFrom AS timestamptz) IS NULL OR e.created >= :createdFrom)
                AND (CAST(:createdTo AS timestamptz) IS NULL OR e.created <= :createdTo)
              GROUP BY pm.value
              ORDER BY total DESC
            """, nativeQuery = true)
    List<Object[]> findPaymentMethodTotals(
            @Param("userId") Long userId,
            @Param("createdFrom") Instant createdFrom,
            @Param("createdTo") Instant createdTo);

    /**
     * Per-day-of-week totals over an optional [from, to] range. ISODOW (1=Monday..7=Sunday)
     * is used rather than DOW (0=Sunday..6=Saturday) so results line up Monday-first with
     * the rest of the response without any remapping in the service layer.
     */
    @Query(value = """
            SELECT EXTRACT(ISODOW FROM e.created)::int AS iso_dow, COALESCE(SUM(e.amount), 0) AS total
            FROM expenses e
            WHERE e.user_id = :userId
                AND (CAST(:createdFrom AS timestamptz) IS NULL OR e.created >= :createdFrom)
                AND (CAST(:createdTo AS timestamptz) IS NULL OR e.created <= :createdTo)
            GROUP BY iso_dow
            ORDER BY iso_dow
            """, nativeQuery = true)
    List<Object[]> findDayOfWeekTotals(
            @Param("userId") Long userId,
            @Param("createdFrom") Instant createdFrom,
            @Param("createdTo") Instant createdTo);

    /**
     * Top spentOn values by total amount over an optional [from, to] range, grouped by
     * the exact spentOn text (no fuzzy/normalized vendor matching).
     */
    @Query(value = """
            SELECT e.spent_on, COALESCE(SUM(e.amount), 0) AS total
            FROM expenses e
            WHERE e.user_id = :userId
              AND e.spent_on IS NOT NULL
              AND (CAST(:createdFrom AS timestamptz) IS NULL OR e.created >= :createdFrom)
              AND (CAST(:createdTo AS timestamptz) IS NULL OR e.created <= :createdTo)
            GROUP BY e.spent_on
            ORDER BY total DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopVendors(
            @Param("userId") Long userId,
            @Param("createdFrom") Instant createdFrom,
            @Param("createdTo") Instant createdTo,
            @Param("limit") int limit);

    /**
     * Single-row KPI aggregate over an optional [from, to] range: [count, sum, avg, max, min].
     * COALESCE guards sum/avg/max/min against null when there are zero matching rows;
     * count(e) is always a non-null 0 in that case already.
     */
    @Query("""
            select count(e), coalesce(sum(e.amount), 0), coalesce(avg(e.amount), 0),
                   coalesce(max(e.amount), 0), coalesce(min(e.amount), 0)
            from Expense e
            where e.user.id = :userId
              and (cast(:createdFrom as timestamp) is null or e.created >= :createdFrom)
              and (cast(:createdTo as timestamp) is null or e.created <= :createdTo)
            """)
    Object[] findSummary(
            @Param("userId") Long userId,
            @Param("createdFrom") Instant createdFrom,
            @Param("createdTo") Instant createdTo);

    /**
     * Grouped daily totals for one calendar year, filtered by a [yearStart, yearEnd)
     * range for the same index-usability reason as findMonthlyTotals above.
     * Returns one row per day that has at least one matching expense — the service
     * layer zero-fills every other day of the year for the heatmap.
     */
    @Query(value = """
            SELECT date_trunc('day', e.created)::date AS day, COALESCE(SUM(e.amount), 0) AS total
            FROM expenses e
            WHERE e.user_id = :userId
              AND e.created >= :yearStart AND e.created < :yearEnd
            GROUP BY day
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> findDailyTotals(
            @Param("userId") Long userId,
            @Param("yearStart") Instant yearStart,
            @Param("yearEnd") Instant yearEnd);
}
