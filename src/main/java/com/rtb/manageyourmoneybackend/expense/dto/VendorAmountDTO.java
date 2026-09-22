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
@Schema(description = "One vendor/merchant and the total spent against them")
public class VendorAmountDTO {

    @Schema(description = "The exact spentOn text this total is grouped by", example = "macbook air m4 | amazon")
    private String spentOn;

    @Schema(description = "Total amount spent against this spentOn value", example = "84900.00")
    private BigDecimal amount;
}