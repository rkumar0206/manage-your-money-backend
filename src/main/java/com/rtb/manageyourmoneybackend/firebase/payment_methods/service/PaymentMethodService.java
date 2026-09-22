package com.rtb.manageyourmoneybackend.firebase.payment_methods.service;

import com.google.firebase.auth.FirebaseAuthException;

public interface PaymentMethodService {

    void syncDataFromFirestore() throws FirebaseAuthException;
}
