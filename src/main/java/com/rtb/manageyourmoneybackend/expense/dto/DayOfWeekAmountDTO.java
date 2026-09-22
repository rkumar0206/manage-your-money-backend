package com.rtb.manageyourmoneybackend.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Total amount spent on one day of the week")
public class DayOfWeekAmountDTO {

    @Schema(description = "Day of week (MONDAY..SUNDAY)")
    private DayOfWeek dayOfWeek;

    @Schema(description = "Short label for display", example = "Mon")
    private String dayLabel;

    @Schema(description = "Total amount spent on this day of week across the range; zero if there's no data", example = "325000.00")
    private BigDecimal amount;
}