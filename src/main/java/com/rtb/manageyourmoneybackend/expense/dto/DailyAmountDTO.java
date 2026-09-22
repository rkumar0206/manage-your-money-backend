package com.rtb.manageyourmoneybackend.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Total amount spent on a single calendar day")
public class DailyAmountDTO {

    @Schema(description = "The calendar day", example = "2026-03-15")
    private LocalDate date;

    @Schema(description = "Total amount spent that day; zero if there were no matching expenses", example = "1250.00")
    private BigDecimal amount;
}