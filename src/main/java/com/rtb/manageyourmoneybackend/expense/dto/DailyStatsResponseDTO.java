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
@Schema(description = "Day-by-day totals for a calendar year, zero-filled for every day (365 or 366 entries depending on leap year) for a heatmap")
public class DailyStatsResponseDTO {

    @Schema(description = "The year these totals cover", example = "2026")
    private int year;

    @Schema(description = "One entry per calendar day of the year, in date order")
    private List<DailyAmountDTO> days;
}