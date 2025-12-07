package com.rtb.manageyourmoneybackend.expense_category.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.expense_category.service.ExpenseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/expense-categories")
@RequiredArgsConstructor
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    @GetMapping("/sync")
    public ResponseEntity<Map<String, String>> syncDataFromFirestore() throws FirebaseAuthException {

        expenseCategoryService.syncDataFromFirestore();

        Map<String, String> response = new HashMap<>();
        response.put("response", "Sync started in background");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
