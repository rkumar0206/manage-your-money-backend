package com.rtb.manageyourmoneybackend.firebase.firebase_expense_category.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.common.enums.FirestoreCollection;
import com.rtb.manageyourmoneybackend.firebase.firebase_expense_category.repository.FirebaseExpenseCategoryRepository;
import com.rtb.manageyourmoneybackend.firebase.firestore.service.FirestoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseExpenseCategoryServiceImpl implements FirebaseExpenseCategoryService {

    private final FirebaseExpenseCategoryRepository firebaseExpenseCategoryRepository;
    private final FirestoreService firestoreService;

    @Override
    public void syncDataFromFirestore() throws FirebaseAuthException {

        firestoreService.syncCollectionData(FirestoreCollection.EXPENSE_CATEGORY);
    }
}
