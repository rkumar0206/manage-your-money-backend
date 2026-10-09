package com.rtb.manageyourmoneybackend.expense.adapter;

import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import com.rtb.manageyourmoneybackend.firebase.models.ExpenseItem;
import com.rtb.manageyourmoneybackend.firebase.models.PaymentMethodItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpenseAdapter {

    public static ExpenseItem toExpenseItem(String uid, Expense expense, List<PaymentMethodItem> paymentMethodItemsFromFirestore) {

        return new ExpenseItem(
                expense.getKey(),
                expense.getAmount(),
                expense.getCategoryKey(),
                expense.getCreated().toEpochMilli(),
                expense.getModified().toEpochMilli(),
                getPaymentMethodKeys(expense.getPaymentMethods(), paymentMethodItemsFromFirestore),
                expense.getSpentOn(),
                true,
                uid
        );
    }

    private static List<String> getPaymentMethodKeys(List<String> paymentMethods, List<PaymentMethodItem> paymentMethodItemsFromFirestore) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            return List.of();
        }

        Set<String> keys = new HashSet<>();

        // 1. Process hardcoded fallback keys directly from paymentMethods
        for (String pm : paymentMethods) {
            if (pm == null) continue;
            String pmLower = pm.toLowerCase();

            if (pmLower.contains("cash")) {
                keys.add("payment_method_cash_1316797_rrrrr");
            }
            if (pmLower.contains("other")) {
                keys.add("payment_method_other_6546332_rrrrr");
            }
            if (pmLower.contains("debit card")) {
                keys.add("payment_method_debit_card_5765763_rrrrr");
            }
            if (pmLower.contains("credit card")) {
                keys.add("payment_method_credit_card_974673_rrrrr");
            }
        }

        // 2. Process dynamic Firestore items
        if (paymentMethodItemsFromFirestore != null) {
            for (PaymentMethodItem item : paymentMethodItemsFromFirestore) {
                if (item != null && item.getPaymentMethod() != null && item.getKey() != null) {
                    String itemMethodLower = item.getPaymentMethod().toLowerCase();

                    for (String pm : paymentMethods) {
                        if (pm != null && itemMethodLower.contains(pm.toLowerCase())) {
                            keys.add(item.getKey());
                            break; // Stops checking further pm keywords for this specific item once matched
                        }
                    }
                }
            }
        }

        return new ArrayList<>(keys);
    }

}
