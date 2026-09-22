package com.rtb.manageyourmoneybackend.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Month-by-month totals for a given year, always 12 entries (zero-filled where there's no data) so charts don't need to backfill gaps themselves")
public class CategoryMonthlyStatsResponseDTO {

    @Schema(description = "The year these totals cover", example = "2026")
    private int year;

    @Schema(description = "Category the totals are scoped to; null means all categories", example = "3")
    private Long categoryId;

    @Schema(description = "Always 12 entries, ordered January through December")
    private List<MonthlyAmountDTO> months;
}