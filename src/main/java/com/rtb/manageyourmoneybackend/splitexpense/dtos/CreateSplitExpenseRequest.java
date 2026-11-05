package com.rtb.manageyourmoneybackend.splitexpense.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSplitExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payer member ID is required")
    private Long paidByMemberId; // Refers to GroupMember.id

    @NotNull(message = "Expense date is required")
    private Instant expenseDate;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    @Valid
    @NotEmpty(message = "At least one share is required")
    private List<SplitShareRequest> shares;
}