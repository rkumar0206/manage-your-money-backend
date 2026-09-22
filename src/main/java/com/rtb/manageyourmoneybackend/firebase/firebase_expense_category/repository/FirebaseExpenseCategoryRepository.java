package com.rtb.manageyourmoneybackend.firebase.firebase_expense_category.repository;

import com.rtb.manageyourmoneybackend.firebase.firebase_expense_category.entity.FirebaseExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirebaseExpenseCategoryRepository extends JpaRepository<FirebaseExpenseCategory, String> {
}
