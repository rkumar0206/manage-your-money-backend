package com.rtb.manageyourmoneybackend.common.cache;

import java.util.Set;

public final class CacheNameConstants {
    private CacheNameConstants() {}

    // User
    public static final String USER = "user";

    // Expense Category caches
    public static final String EXPENSE_CATEGORY_BY_ID = "expenseCategoryById";
    public static final String EXPENSE_CATEGORY_LIST  = "expenseCategoryList";

    // Expense caches
    public static final String EXPENSE_BY_ID       = "expenseById";
    public static final String EXPENSE_LIST        = "expenseList";
    public static final String EXPENSE_AGGREGATES  = "expenseAggregates";
    public static final String EXPENSE_PAYMENT_METHODS = "expensePaymentMethods";
    public static final String EXPENSE_DISTINCT_SPENT_ON =  "expenseDistinctSpentOn";
    public static final String PAYMENT_METHODS_FIRESTORE = "paymentMethodsFiresStore";

    // Unsplash
    public static final String UNSPLASH_SEARCH_RESULT = "unsplashSearchResult";

    // Firebase UID
    public static final String UID = "uid";

    /** Used to validate YAML config at startup. Keep in sync with the fields above. */
    public static final Set<String> ALL = Set.of(
            USER,
            EXPENSE_CATEGORY_BY_ID,
            EXPENSE_CATEGORY_LIST,
            EXPENSE_BY_ID,
            EXPENSE_LIST,
            EXPENSE_AGGREGATES,
            EXPENSE_PAYMENT_METHODS,
            EXPENSE_DISTINCT_SPENT_ON,
            PAYMENT_METHODS_FIRESTORE,
            UNSPLASH_SEARCH_RESULT,
            UID
    );
}
