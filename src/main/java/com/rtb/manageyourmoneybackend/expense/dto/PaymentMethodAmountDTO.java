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
@Schema(description = "Total amount attributed to one payment method over the requested range")
public class PaymentMethodAmountDTO {

    @Schema(description = "Payment method label, exactly as stored on the expense", example = "HDFC")
    private String paymentMethod;

    @Schema(description = "Total amount attributed to this payment method", example = "1520000.00")
    private BigDecimal amount;
}