package com.rtb.manageyourmoneybackend.splitexpense.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBalancesResponse {
    private Long groupId;
    private List<MemberBalance> memberBalances;
    private List<SettlementTransaction> suggestedSettlements;
}