package com.rtb.manageyourmoneybackend.firebase.payment_methods.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "payment_method")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @Column(name = "key", nullable = false, unique = true)
    private String key;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "is_synced")
    private Boolean isSynced;

    @Column(name = "uid", nullable = false)
    private String uid;
}
