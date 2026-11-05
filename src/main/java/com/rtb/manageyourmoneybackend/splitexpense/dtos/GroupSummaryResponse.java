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
public class GroupSummaryResponse {
    private Long id;
    private String name;
    private String description;
    private int memberCount;
    private BigDecimal totalSpent;
    private BigDecimal userNetBalance; // Positive = you are owed, Negative = you owe
}