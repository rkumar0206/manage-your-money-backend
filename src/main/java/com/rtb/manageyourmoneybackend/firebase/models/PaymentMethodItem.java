package com.rtb.manageyourmoneybackend.firebase.models;

import com.rtb.manageyourmoneybackend.firebase.annotations.FirestoreCollection;
import com.rtb.manageyourmoneybackend.firebase.annotations.FirestoreId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FirestoreCollection("PaymentMethods")
public class PaymentMethodItem {
    @FirestoreId
    private String key;
    private String paymentMethod;
    private boolean selected;
    private boolean synced;
    private String uid;
}
