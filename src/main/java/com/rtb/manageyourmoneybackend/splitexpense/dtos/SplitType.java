package com.rtb.manageyourmoneybackend.splitexpense.dtos;

public enum SplitType {
    EQUAL,      // Divided equally among participants
    EXACT,      // Exact amounts provided for each participant
    PERCENTAGE  // Percentages provided for each participant (must total 100)
}