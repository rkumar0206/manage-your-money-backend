package com.rtb.manageyourmoneybackend.splitexpense.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SplitShareRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId; // Refers to GroupMember.id

    /**
     * How this is interpreted depends on the SplitType:
     * - EQUAL: Can be null/ignored (backend calculates).
     * - EXACT: The exact monetary amount this member owes.
     * - PERCENTAGE: The percentage (e.g., 25.5 for 25.5%) this member owes.
     */
    private BigDecimal amount;
}