package com.rtb.manageyourmoneybackend.expense_category.service;

import com.google.firebase.auth.FirebaseAuthException;


public interface ExpenseCategoryService {

    void syncDataFromFirestore() throws FirebaseAuthException;
}
