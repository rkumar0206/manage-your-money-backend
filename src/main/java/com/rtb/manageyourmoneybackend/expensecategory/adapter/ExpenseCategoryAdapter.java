package com.rtb.manageyourmoneybackend.expensecategory.adapter;

import com.rtb.manageyourmoneybackend.expensecategory.entity.ExpenseCategory;
import com.rtb.manageyourmoneybackend.firebase.models.CategoryItem;

public class ExpenseCategoryAdapter {

    public static CategoryItem toCategoryItem(String uid, ExpenseCategory expenseCategory) {

        return new CategoryItem(
                expenseCategory.getKey(),
                expenseCategory.getDescription(),
                expenseCategory.getName(),
                expenseCategory.getCreated().toEpochMilli(),
                expenseCategory.getImageUrl(),
                expenseCategory.getModified().toEpochMilli(),
                true,
                uid
        );
    }
}
