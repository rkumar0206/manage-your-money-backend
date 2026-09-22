package com.rtb.manageyourmoneybackend.expensecategory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseCategoryDTO {

    private Long id;

    @NotBlank(message = "Category name is mandatory")
    @Size(max = 255, message = "Category name must be less than 255 characters")
    private String name;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    @Size(max = 255, message = "Image URL must be less than 255 characters")
    private String imageUrl;

    private boolean isSynced;

    // Included for GET responses. Ignored during POST/PUT request body binding.
    private Long userId;
}