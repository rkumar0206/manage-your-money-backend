package com.rtb.manageyourmoneybackend.common.cache;

import java.util.Set;

public final class CacheNameConstants {
    private CacheNameConstants() {}

    // Expense Category caches
    public static final String EXPENSE_CATEGORY_BY_ID = "expenseCategoryById";
    public static final String EXPENSE_CATEGORY_LIST  = "expenseCategoryList";

    // Expense caches
    public static final String EXPENSE_BY_ID       = "expenseById";
    public static final String EXPENSE_LIST        = "expenseList";
    public static final String EXPENSE_AGGREGATES  = "expenseAggregates";
    public static final String EXPENSE_PAYMENT_METHODS = "expensePaymentMethods";

    /** Used to validate YAML config at startup. Keep in sync with the fields above. */
    public static final Set<String> ALL = Set.of(
            EXPENSE_CATEGORY_BY_ID,
            EXPENSE_CATEGORY_LIST,
            EXPENSE_BY_ID,
            EXPENSE_LIST,
            EXPENSE_AGGREGATES,
            EXPENSE_PAYMENT_METHODS
    );
}
