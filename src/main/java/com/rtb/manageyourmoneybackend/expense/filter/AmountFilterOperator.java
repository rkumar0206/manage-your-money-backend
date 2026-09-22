package com.rtb.manageyourmoneybackend.expense.filter;

/**
 * How {@code amount} should be compared against the value(s) supplied in a search request.
 */
public enum AmountFilterOperator {
    IS_EQUALS_TO,
    IS_LESS_THAN,
    IS_GREATER_THAN,
    IS_BETWEEN
}