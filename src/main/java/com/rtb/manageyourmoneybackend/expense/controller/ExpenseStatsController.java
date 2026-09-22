package com.rtb.manageyourmoneybackend.expense.controller;

import com.rksdev.security.web.CurrentUserId;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryBreakdownResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryMonthlyStatsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryStatsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.DailyStatsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.DayOfWeekStatsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseSummaryResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.PaymentMethodDistributionResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.TopVendorsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
import com.rtb.manageyourmoneybackend.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only aggregate endpoints intended for graphs/dashboards, kept separate
 * from ExpenseController's CRUD + search endpoints for clarity.
 */
@RestController
@RequestMapping("/api/v1/expenses/stats")
@RequiredArgsConstructor
@Tag(name = "Expense Stats", description = "Aggregate expense data for charts and dashboards")
public class ExpenseStatsController {

    private final ExpenseService expenseService;

    @GetMapping("/category-total")
    @Operation(summary = "Total amount spent in a category over a predefined date range",
            description = "paymentMethods is optional and matches ANY of the given values.")
    @ApiResponse(responseCode = "200", description = "Total retrieved successfully")
    public ResponseEntity<CategoryStatsResponseDTO> getCategoryTotalForDateRange(
            @CurrentUserId Long userId,
            @Parameter(description = "Category to total", required = true)
            @RequestParam Long categoryId,
            @Parameter(description = "Predefined date range", required = true)
            @RequestParam DateRangePreset dateRangePreset,
            @Parameter(description = "Optional: restrict to expenses using any of these payment methods")
            @RequestParam(required = false) List<String> paymentMethods) {
        return ResponseEntity.ok(
                expenseService.getCategoryTotalForDateRange(userId, categoryId, dateRangePreset, paymentMethods));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Month-by-month totals for a given year",
            description = "categoryId and paymentMethods are optional filters. Always returns 12 entries "
                    + "(zero-filled where there's no data) so charts don't need to backfill gaps themselves.")
    @ApiResponse(responseCode = "200", description = "Monthly totals retrieved successfully")
    public ResponseEntity<CategoryMonthlyStatsResponseDTO> getMonthlyTotals(
            @CurrentUserId Long userId,
            @Parameter(description = "Calendar year to total", required = true, example = "2026")
            @RequestParam int year,
            @Parameter(description = "Optional: restrict to a single category")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Optional: restrict to expenses using any of these payment methods")
            @RequestParam(required = false) List<String> paymentMethods) {
        return ResponseEntity.ok(expenseService.getMonthlyTotalsForYear(userId, year, categoryId, paymentMethods));
    }

    @GetMapping("/category-breakdown")
    @Operation(summary = "Category-wise breakdown of spend over a predefined date range",
            description = "Backs both the \"Category Breakdown\" (dateRangePreset=ALL_TIME) and \"Current Month "
                    + "Category Spend\" (dateRangePreset=THIS_MONTH) widgets — same endpoint, different preset. "
                    + "Pass topN to fold every category beyond the top N (by amount) into a single trailing "
                    + "\"Other\" entry, computed server-side.")
    @ApiResponse(responseCode = "200", description = "Category breakdown retrieved successfully")
    public ResponseEntity<CategoryBreakdownResponseDTO> getCategoryBreakdown(
            @CurrentUserId Long userId,
            @Parameter(description = "Predefined date range; defaults to ALL_TIME")
            @RequestParam(required = false, defaultValue = "ALL_TIME") DateRangePreset dateRangePreset,
            @Parameter(description = "Optional: keep only the top N categories by amount, folding the rest into \"Other\"")
            @RequestParam(required = false) Integer topN) {
        return ResponseEntity.ok(expenseService.getCategoryBreakdown(userId, dateRangePreset, topN));
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Spend broken down by payment method over a predefined date range")
    @ApiResponse(responseCode = "200", description = "Payment method distribution retrieved successfully")
    public ResponseEntity<PaymentMethodDistributionResponseDTO> getPaymentMethodDistribution(
            @CurrentUserId Long userId,
            @Parameter(description = "Predefined date range; defaults to ALL_TIME")
            @RequestParam(required = false, defaultValue = "ALL_TIME") DateRangePreset dateRangePreset) {
        return ResponseEntity.ok(expenseService.getPaymentMethodDistribution(userId, dateRangePreset));
    }

    @GetMapping("/day-of-week")
    @Operation(summary = "Spend broken down by day of week over a predefined date range",
            description = "Always returns 7 entries (zero-filled where there's no data), Monday through Sunday.")
    @ApiResponse(responseCode = "200", description = "Day-of-week breakdown retrieved successfully")
    public ResponseEntity<DayOfWeekStatsResponseDTO> getDayOfWeekStats(
            @CurrentUserId Long userId,
            @Parameter(description = "Predefined date range; defaults to ALL_TIME")
            @RequestParam(required = false, defaultValue = "ALL_TIME") DateRangePreset dateRangePreset) {
        return ResponseEntity.ok(expenseService.getDayOfWeekStats(userId, dateRangePreset));
    }

    @GetMapping("/top-vendors")
    @Operation(summary = "Top spentOn values by total amount over a predefined date range",
            description = "Grouped by the exact spentOn text (no fuzzy vendor matching).")
    @ApiResponse(responseCode = "200", description = "Top vendors retrieved successfully")
    public ResponseEntity<TopVendorsResponseDTO> getTopVendors(
            @CurrentUserId Long userId,
            @Parameter(description = "Predefined date range; defaults to ALL_TIME")
            @RequestParam(required = false, defaultValue = "ALL_TIME") DateRangePreset dateRangePreset,
            @Parameter(description = "Max number of vendors to return; defaults to 5")
            @RequestParam(required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(expenseService.getTopVendors(userId, dateRangePreset, limit));
    }

    @GetMapping("/summary")
    @Operation(summary = "Single-call KPI summary (total, count, average, largest, smallest) over a predefined date range",
            description = "Replaces separately fetching each KPI card's value; avgAmount is computed server-side "
                    + "and rounded to 2 decimal places.")
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    public ResponseEntity<ExpenseSummaryResponseDTO> getSummary(
            @CurrentUserId Long userId,
            @Parameter(description = "Predefined date range; defaults to ALL_TIME")
            @RequestParam(required = false, defaultValue = "ALL_TIME") DateRangePreset dateRangePreset) {
        return ResponseEntity.ok(expenseService.getSummary(userId, dateRangePreset));
    }

    @GetMapping("/daily")
    @Operation(summary = "Day-by-day totals for a calendar year, for a heatmap",
            description = "Always returns 365 (or 366 in a leap year) entries, zero-filled where there's no data.")
    @ApiResponse(responseCode = "200", description = "Daily totals retrieved successfully")
    public ResponseEntity<DailyStatsResponseDTO> getDailyStats(
            @CurrentUserId Long userId,
            @Parameter(description = "Calendar year to total", required = true, example = "2026")
            @RequestParam int year) {
        return ResponseEntity.ok(expenseService.getDailyStats(userId, year));
    }
}