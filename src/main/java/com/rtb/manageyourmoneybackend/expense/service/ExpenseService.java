package com.rtb.manageyourmoneybackend.expense.service;

import com.google.firebase.auth.FirebaseAuthException;


public interface ExpenseService {

    void syncDataFromFirestore() throws FirebaseAuthException;
}
