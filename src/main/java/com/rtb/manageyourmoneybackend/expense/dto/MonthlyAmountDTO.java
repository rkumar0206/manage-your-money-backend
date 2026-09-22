package com.rtb.manageyourmoneybackend.expense.dto;

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
@Schema(description = "Total amount spent in a single calendar month")
public class MonthlyAmountDTO {

    @Schema(description = "Month number, 1 (January) through 12 (December)", example = "3")
    private int month;

    @Schema(description = "Full month name", example = "March")
    private String monthName;

    @Schema(description = "Total amount spent that month; zero if there were no matching expenses", example = "430.50")
    private BigDecimal amount;
}