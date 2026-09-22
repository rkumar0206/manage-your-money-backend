package com.rtb.manageyourmoneybackend.firebase.payment_methods.repository;

import com.rtb.manageyourmoneybackend.firebase.payment_methods.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {


}
