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
 * Payload used to create a new {@code ExpenseCategory}.
 * <p>
 * The owning {@code User} (the {@code @ManyToOne} parent) is deliberately
 * excluded from this body — it is resolved from the path variable in the
 * controller, per the parent-association handling rules. There is no
 * {@code @OneToMany} collection on this entity, so nothing further is excluded.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ExpenseCategoryCreateRequest", description = "Payload for creating a new expense category")
public class ExpenseCategoryCreateRequestDTO {

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

    private Instant created;
    private Instant modified;
}
