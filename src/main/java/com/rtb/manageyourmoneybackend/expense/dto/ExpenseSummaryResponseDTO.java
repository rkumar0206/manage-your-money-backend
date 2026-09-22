package com.rtb.manageyourmoneybackend.expense.dto;

import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Single-call KPI summary over a predefined date range")
public class ExpenseSummaryResponseDTO {

    @Schema(description = "The date range this summary covers")
    private DateRangePreset dateRangePreset;

    @Schema(description = "Sum of all matching expenses", example = "2870000.00")
    private BigDecimal totalAmount;

    @Schema(description = "Number of matching expenses", example = "142")
    private long totalCount;

    @Schema(description = "Average expense amount, rounded to 2 decimal places; zero if there are no matching expenses", example = "20211.27")
    private BigDecimal avgAmount;

    @Schema(description = "Largest single expense amount in the range; zero if there are no matching expenses", example = "156000.00")
    private BigDecimal largestExpense;

    @Schema(description = "Smallest single expense amount in the range; zero if there are no matching expenses", example = "50.00")
    private BigDecimal minAmount;
}