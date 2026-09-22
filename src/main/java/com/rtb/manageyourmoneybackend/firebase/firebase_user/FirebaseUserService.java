package com.rtb.manageyourmoneybackend.firebase.firebase_user;

import com.google.firebase.auth.FirebaseAuthException;

public interface FirebaseUserService {

    String getUidSecurityContext() throws FirebaseAuthException;
}
