package com.rtb.manageyourmoneybackend.expense.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.common.enums.FirestoreCollection;
import com.rtb.manageyourmoneybackend.expense.repository.ExpenseRepository;
import com.rtb.manageyourmoneybackend.expense_category.repository.ExpenseCategoryRepository;
import com.rtb.manageyourmoneybackend.firebase.firestore.service.FirestoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final FirestoreService firestoreService;

    @Override
    public void syncDataFromFirestore() throws FirebaseAuthException {

        firestoreService.syncCollectionData(FirestoreCollection.EXPENSE);
    }
}
