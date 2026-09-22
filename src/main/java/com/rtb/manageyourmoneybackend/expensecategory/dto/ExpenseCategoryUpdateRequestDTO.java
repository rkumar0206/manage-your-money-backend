package com.rtb.manageyourmoneybackend.expensecategory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Payload used to update an existing {@code ExpenseCategory}.
 * <p>
 * The entity {@code id} is supplied via path variable, not in the body.
 * The owning {@code User} is immutable through this endpoint; re-parenting
 * a category to a different user is intentionally out of scope here.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ExpenseCategoryUpdateRequest", description = "Payload for updating an existing expense category")
public class ExpenseCategoryUpdateRequestDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "Display name of the expense category", example = "Groceries", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Schema(description = "Optional free-text description", example = "Everyday grocery and household spending")
    private String description;

    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    @Schema(description = "URL of an icon/image representing the category", example = "https://cdn.example.com/icons/groceries.png")
    private String imageUrl;

    @NotNull(message = "isSynced must be provided")
    @Schema(description = "Whether this category has been synced with the client", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isSynced;

    private Instant created;
}
