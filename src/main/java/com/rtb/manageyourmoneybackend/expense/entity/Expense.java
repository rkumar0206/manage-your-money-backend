package com.rtb.manageyourmoneybackend.expense.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "expense")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Expense {

    @Id
    @Column(name = "key", nullable = false, unique = true)
    private String key;

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "category_key", nullable = false)
    private String categoryKey;

    @Column(name = "spent_on")
    private String spentOn;

    @Column(name = "payment_methods")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expense_payment_methods", joinColumns = @JoinColumn(name = "expense_key"))
    private List<String> paymentMethods;

    @Column(name = "is_synced")
    private Boolean isSynced;

    @Column(name = "uid", nullable = false)
    private String uid;

    @Column(name = "created", nullable = false)
    private Long created;

    @Column(name = "modified", nullable = false)
    private Long modified;

}
