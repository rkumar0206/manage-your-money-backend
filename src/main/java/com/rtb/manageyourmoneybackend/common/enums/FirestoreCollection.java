package com.rtb.manageyourmoneybackend.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FirestoreCollection {

    EXPENSE_CATEGORY("ExpenseCategories"),
    EXPENSE("Expenses"),
    PAYMENT_METHOD("PaymentMethods");

    private final String collectionName;
}
