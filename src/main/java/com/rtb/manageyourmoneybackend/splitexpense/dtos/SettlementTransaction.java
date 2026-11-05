package com.rtb.manageyourmoneybackend.splitexpense.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementTransaction {
    private Long fromMemberId;
    private String fromName;
    private Long toMemberId;
    private String toName;
    private BigDecimal amount;
}