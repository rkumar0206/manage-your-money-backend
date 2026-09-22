package com.rtb.manageyourmoneybackend.firebase.payment_methods.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.common.enums.FirestoreCollection;
import com.rtb.manageyourmoneybackend.firebase.firestore.service.FirestoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentMethodServiceImpl implements PaymentMethodService {

    private final FirestoreService firestoreService;

    @Override
    public void syncDataFromFirestore() throws FirebaseAuthException {
        firestoreService.syncCollectionData(FirestoreCollection.PAYMENT_METHOD);
    }
}
