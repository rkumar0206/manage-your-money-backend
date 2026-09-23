package com.rtb.manageyourmoneybackend.common.config;

public final class CacheConstants {
    private CacheConstants() {}

    // Expense Category caches
    public static final String EXPENSE_CATEGORY_BY_ID = "expenseCategoryById";
    public static final String EXPENSE_CATEGORY_LIST  = "expenseCategoryList";

    // Expense caches
    public static final String EXPENSE_BY_ID       = "expenseById";
    public static final String EXPENSE_LIST        = "expenseList";
    public static final String EXPENSE_AGGREGATES  = "expenseAggregates";
    public static final String EXPENSE_PAYMENT_METHODS = "expensePaymentMethods";
}
