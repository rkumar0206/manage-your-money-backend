package com.rtb.manageyourmoneybackend.splitexpense.dtos;

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
public class SplitExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private Long paidByMemberId;
    private String paidByMemberName;
    private Instant expenseDate;
    private List<ShareDto> shares;
}