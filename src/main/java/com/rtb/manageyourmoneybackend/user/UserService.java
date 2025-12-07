package com.rtb.manageyourmoneybackend.user;

import com.google.firebase.auth.FirebaseAuthException;

public interface UserService {

    String getUidSecurityContext() throws FirebaseAuthException;
}
