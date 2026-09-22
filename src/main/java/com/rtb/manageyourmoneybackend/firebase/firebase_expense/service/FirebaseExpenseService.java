package com.rtb.manageyourmoneybackend.firebase.firebase_expense.service;

import com.google.firebase.auth.FirebaseAuthException;


public interface FirebaseExpenseService {

    void syncDataFromFirestore() throws FirebaseAuthException;
}
