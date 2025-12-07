package com.rtb.manageyourmoneybackend.expense.repository;

import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
}
