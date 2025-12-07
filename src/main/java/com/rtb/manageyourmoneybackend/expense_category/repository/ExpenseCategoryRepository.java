package com.rtb.manageyourmoneybackend.expense_category.repository;

import com.rtb.manageyourmoneybackend.expense_category.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, String> {
}
