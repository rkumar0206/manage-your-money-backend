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
@Schema(description = "Total amount spent in a category over a given predefined date range")
public class CategoryStatsResponseDTO {

    @Schema(description = "Category the total was computed for", example = "3")
    private Long categoryId;

    @Schema(description = "Date range the total was computed over")
    private DateRangePreset dateRangePreset;

    @Schema(description = "Total amount spent, zero if there were no matching expenses", example = "1250.00")
    private BigDecimal totalAmount;
}