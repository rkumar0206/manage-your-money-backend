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
@Schema(description = "One category's share of total spend over the requested range")
public class CategoryBreakdownItemDTO {

    @Schema(description = "Category id", example = "3")
    private Long categoryId;

    @Schema(description = "Category name", example = "Shopping")
    private String categoryName;

    @Schema(description = "Total amount spent in this category over the range", example = "295620.66")
    private BigDecimal amount;

    @Schema(description = "This category's percentage of the grand total, rounded to 2 decimal places", example = "10.00")
    private BigDecimal percentage;
}
