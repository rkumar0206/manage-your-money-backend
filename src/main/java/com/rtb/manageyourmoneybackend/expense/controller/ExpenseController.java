package com.rtb.manageyourmoneybackend.expense.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("/sync")
    public ResponseEntity<Map<String, String>> syncDataFromFirestore() throws FirebaseAuthException {

        expenseService.syncDataFromFirestore();

        Map<String, String> response = new HashMap<>();
        response.put("response", "Sync started in background");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

