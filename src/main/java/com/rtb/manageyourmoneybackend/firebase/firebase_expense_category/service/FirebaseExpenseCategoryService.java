package com.rtb.manageyourmoneybackend.firebase.firebase_expense_category.service;

import com.google.firebase.auth.FirebaseAuthException;


public interface FirebaseExpenseCategoryService {

    void syncDataFromFirestore() throws FirebaseAuthException;
}
