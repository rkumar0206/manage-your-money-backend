package com.rtb.manageyourmoneybackend.firebase.firebase_expense_category.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "expense_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseExpenseCategory {

    @Id
    @Column(name = "key", nullable = false, unique = true)
    private String key;

    @Column(name = "category_description")
    private String categoryDescription;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_synced")
    private Boolean isSynced;

    @Column(name = "uid", nullable = false)
    private String uid;

    @Column(name = "created", nullable = false)
    private Long created;

    @Column(name = "modified", nullable = false)
    private Long modified;
}
