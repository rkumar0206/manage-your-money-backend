package com.rtb.manageyourmoneybackend.splitexpense.dtos;

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
public class SettlementResponse {
    private Long id;
    private Long paidByMemberId;
    private String paidByMemberName;
    private Long paidToMemberId;
    private String paidToMemberName;
    private BigDecimal amount;
    private Instant settlementDate;
}