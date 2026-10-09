package com.rtb.manageyourmoneybackend.firebase.models;

import com.rtb.manageyourmoneybackend.firebase.annotations.FirestoreCollection;
import com.rtb.manageyourmoneybackend.firebase.annotations.FirestoreId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FirestoreCollection("Expenses")
public class ExpenseItem {

    @FirestoreId
    private String key;
    private BigDecimal amount;
    private String categoryKey;
    private long created;
    private long modified;
    private List<String> paymentMethods;
    private String spentOn;
    private boolean synced;
    private String uid;
}
