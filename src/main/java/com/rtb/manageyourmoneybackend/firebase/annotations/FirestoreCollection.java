package com.rtb.manageyourmoneybackend.firebase.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FirestoreCollection {
    String value();
}