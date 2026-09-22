package com.rtb.manageyourmoneybackend.expensecategory.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TempExpenseCategoryServiceImpl {


/*
    Map<String, ExpenseCategory> categoryExpenseRel =  new HashMap<>();

    public void migrateFirebaseDataToExpenseCategory() {

        UserEntity userEntity = userRepository.findById(1L).get();

        List<FirebaseExpenseCategory> all = firebaseExpenseCategoryRepository.findAll();

        List<ExpenseCategory> expenseCategories = new ArrayList<>();

        for (FirebaseExpenseCategory cat : all) {

            ExpenseCategory category = ExpenseCategory.builder()
                    .name(cat.getCategoryName())
                    .description(cat.getCategoryDescription())
                    .imageUrl(cat.getImageUrl())
                    .isSynced(true)
                    .user(userEntity)
                    .created(Instant.ofEpochMilli(cat.getCreated()))
                    .modified(Instant.ofEpochMilli(cat.getModified()))
                    .build();

            expenseCategories.add(category);

            categoryExpenseRel.put(cat.getKey(), category);
        }

        expenseCategoryRepository.saveAllAndFlush(expenseCategories);

        List<FirebaseExpense> expenseAll = firebaseExpenseRepository.findAll();

        List<PaymentMethod> paymentMethodAll = paymentMethodRepository.findAll();

        Map<String, String> paymentMethodMap = paymentMethodAll.stream()
                .collect(Collectors.toMap(PaymentMethod::getKey, PaymentMethod::getPaymentMethod));

        paymentMethodMap.put("payment_method_other_6546332_rrrrr", "OTHER");
        paymentMethodMap.put("payment_method_cash_1316797_rrrrr", "CASH");

        List<Expense> expenses =  expenseAll.stream()
                .map(exp -> Expense.builder()
                        .amount(BigDecimal.valueOf(exp.getAmount()))
                        .spentOn(exp.getSpentOn())
                        .category(categoryExpenseRel.get(exp.getCategoryKey()))
                        .user(userEntity)
                        .isSynced(true)
                        .created(Instant.ofEpochMilli(exp.getCreated()))
                        .modified(Instant.ofEpochMilli(exp.getModified()))
                        .paymentMethods(
                               exp.getPaymentMethods().stream().map(paymentMethodMap::get).toList()
                        )
                        .build())
                .toList();

        expenseRepository.saveAllAndFlush(expenses);
    }
*/
}
