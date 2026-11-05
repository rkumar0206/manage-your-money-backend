package com.rtb.manageyourmoneybackend.splitexpense.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSettlementRequest {

    @NotNull(message = "Payer member ID is required")
    private Long paidByMemberId; // Refers to GroupMember.id

    @NotNull(message = "Payee member ID is required")
    private Long paidToMemberId; // Refers to GroupMember.id

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Settlement date is required")
    private Instant settlementDate;
}