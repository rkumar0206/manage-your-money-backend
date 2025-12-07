package com.rtb.manageyourmoneybackend.firebase.firestore.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.common.enums.FirestoreCollection;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface FirestoreService {

   void syncCollectionData(FirestoreCollection firestoreCollection) throws FirebaseAuthException;
}
