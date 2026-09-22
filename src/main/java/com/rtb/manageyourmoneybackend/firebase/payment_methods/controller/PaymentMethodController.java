package com.rtb.manageyourmoneybackend.firebase.payment_methods.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.firebase.payment_methods.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment_methods")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping("/sync")
    public ResponseEntity<Map<String, String>> syncDataFromFirestore() throws FirebaseAuthException {

        paymentMethodService.syncDataFromFirestore();

        Map<String, String> response = new HashMap<>();
        response.put("response", "Sync started in background");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
