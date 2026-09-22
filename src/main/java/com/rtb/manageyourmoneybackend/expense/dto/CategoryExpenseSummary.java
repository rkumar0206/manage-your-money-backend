package com.rtb.manageyourmoneybackend.expense.dto;

import java.math.BigDecimal;

public record CategoryExpenseSummary(
        Long categoryId,
        BigDecimal totalAmount
) {}
