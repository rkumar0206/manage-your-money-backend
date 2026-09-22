package com.rtb.manageyourmoneybackend.firebase.firebase_expense.repository;

import com.rtb.manageyourmoneybackend.firebase.firebase_expense.entity.FirebaseExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FirebaseExpenseRepository extends JpaRepository<FirebaseExpense, String> {
}
