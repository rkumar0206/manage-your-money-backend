package com.rtb.manageyourmoneybackend.firebase.models;

import com.rtb.manageyourmoneybackend.firebase.annotations.FirestoreCollection;
import com.rtb.manageyourmoneybackend.firebase.annotations.FirestoreId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FirestoreCollection("ExpenseCategories")
public class CategoryItem {

    @FirestoreId
    private String key;
    private String categoryDescription;
    private String categoryName;
    private long created;
    private String imageUrl;
    private long modified;
    private boolean synced;
    private String uid;
}
