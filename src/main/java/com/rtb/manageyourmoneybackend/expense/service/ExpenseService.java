package com.rtb.manageyourmoneybackend.expense.service;

import com.rtb.manageyourmoneybackend.common.model.PageResponse;
import com.rtb.manageyourmoneybackend.expense.dto.*;
import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ExpenseService {

    ExpenseResponseDTO create(Long userId, ExpenseCreateRequestDTO request);

    ExpenseResponseDTO getById(Long userId, Long id);

    PageResponse<ExpenseResponseDTO> getAll(Long userId, Pageable pageable);

    PageResponse<ExpenseResponseDTO> getAllByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    ExpenseResponseDTO update(Long userId, Long id, ExpenseUpdateRequestDTO request);

    void delete(Long userId, Long id);

    /**
     * Returns the distinct, sorted set of payment methods used across
     * all of the current user's expenses.
     */
    PaymentMethodsResponse getDistinctPaymentMethods(Long userId);

    /**
     * Total amount spent by the user within a single category. Returns
     * zero (never null) when there are no matching expenses.
     */
    BigDecimal getTotalAmountSpentByCategoryId(Long userId, Long categoryId);

    /**
     * Total amount spent by the user across all categories. Returns
     * zero (never null) when the user has no expenses.
     */
    BigDecimal getTotalAmountSpentByUserId(Long userId);

    /**
     * Dynamic filtered search over the current user's expenses.
     */
    PageResponse<ExpenseResponseDTO> search(Long userId, ExpenseSearchRequestDTO criteria, Pageable pageable);

    /**
     * Total amount for the same filters as {@link #search}, unpaginated.
     */
    BigDecimal getTotalAmountSpentBySearchCriteria(Long userId, ExpenseSearchRequestDTO criteria);

    /**
     * Convenience wrapper over {@link #getTotalAmountSpentBySearchCriteria} for
     * a single category over a predefined date range — for a stats/graph widget
     * that doesn't need the full search request shape.
     */
    CategoryStatsResponseDTO getCategoryTotalForDateRange(
            Long userId, Long categoryId, DateRangePreset dateRangePreset, List<String> paymentMethods);

    /**
     * Month-by-month totals for a given year, always 12 entries (zero-filled
     * where there's no data). categoryId and paymentMethods are optional filters.
     */
    CategoryMonthlyStatsResponseDTO getMonthlyTotalsForYear(
            Long userId, int year, Long categoryId, List<String> paymentMethods);

    /**
     * Category-wise breakdown of spend over a predefined date range. Backs both
     * the "Category Breakdown" (typically ALL_TIME) and "Current Month Category
     * Spend" (THIS_MONTH) widgets — same query, different preset.
     * <p>
     * When {@code topN} is provided (and there are more than topN categories with
     * spend in range), only the top topN categories by amount are returned as-is;
     * every category beyond that is folded into a single trailing "Other" entry
     * (categoryId null, categoryName "Other") so percentages still sum to ~100%.
     * Pass null to disable this and return every category individually.
     */
    CategoryBreakdownResponseDTO getCategoryBreakdown(Long userId, DateRangePreset dateRangePreset, Integer topN);

    /**
     * Spend broken down by payment method over a predefined date range.
     */
    PaymentMethodDistributionResponseDTO getPaymentMethodDistribution(Long userId, DateRangePreset dateRangePreset);

    /**
     * Spend broken down by day of week over a predefined date range, always
     * 7 entries (zero-filled), Monday through Sunday.
     */
    DayOfWeekStatsResponseDTO getDayOfWeekStats(Long userId, DateRangePreset dateRangePreset);

    /**
     * Top spentOn values by total amount over a predefined date range.
     */
    TopVendorsResponseDTO getTopVendors(Long userId, DateRangePreset dateRangePreset, int limit);

    /**
     * Single-call KPI summary (total/count/avg/largest/min) over a predefined date range.
     */
    ExpenseSummaryResponseDTO getSummary(Long userId, DateRangePreset dateRangePreset);

    /**
     * Day-by-day totals for a calendar year, zero-filled for every day (365 or 366
     * entries depending on leap year), for a heatmap-style visualization.
     */
    DailyStatsResponseDTO getDailyStats(Long userId, int year);
}