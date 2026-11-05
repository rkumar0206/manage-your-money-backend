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
public class ShareDto {
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
}