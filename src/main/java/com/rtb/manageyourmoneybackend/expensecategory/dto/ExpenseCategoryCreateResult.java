package com.rtb.manageyourmoneybackend.expensecategory.dto;

/**
 * Outcome of an idempotent create call.
 *
 * @param data         the resulting (either newly-created or pre-existing) category
 * @param newlyCreated {@code true} if this call actually inserted a new row;
 *                     {@code false} if an existing category with the same
 *                     (case-insensitive) name for this user was returned instead
 */
public record ExpenseCategoryCreateResult(ExpenseCategoryResponseDTO data, boolean newlyCreated) {
}
