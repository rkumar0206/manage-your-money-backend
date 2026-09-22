package com.rtb.manageyourmoneybackend.expense.dto;

import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
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
@Schema(description = "Spend broken down by day of week, always 7 entries (zero-filled), Monday through Sunday")
public class DayOfWeekStatsResponseDTO {

    @Schema(description = "The date range these totals cover")
    private DateRangePreset dateRangePreset;

    @Schema(description = "Always 7 entries, ordered Monday through Sunday")
    private List<DayOfWeekAmountDTO> days;
}