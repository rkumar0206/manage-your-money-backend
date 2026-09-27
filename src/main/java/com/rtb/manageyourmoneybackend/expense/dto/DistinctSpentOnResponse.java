package com.rtb.manageyourmoneybackend.expense.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistinctSpentOnResponse {
    private List<String> spentOn;
}
