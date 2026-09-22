package com.rtb.manageyourmoneybackend.expensecategory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read-model returned to clients. The {@code @ManyToOne} parent is flattened
 * to its identifier ({@code userId}) rather than exposing the full nested
 * {@code UserEntity} graph.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ExpenseCategoryResponse", description = "Representation of an expense category returned by the API")
public class ExpenseCategoryResponseDTO {

    @Schema(description = "Unique identifier", example = "42")
    private Long id;

    @Schema(description = "Display name of the expense category", example = "Groceries")
    private String name;

    @Schema(description = "Optional free-text description", example = "Everyday grocery and household spending")
    private String description;

    @Schema(description = "URL of an icon/image representing the category", example = "https://cdn.example.com/icons/groceries.png")
    private String imageUrl;

    @Schema(description = "Total expense amount sum added to this category", example = "8600.0")
    private BigDecimal totalExpenseAmount;

    @Schema(description = "Whether this category has been synced with the client", example = "true")
    private boolean isSynced;

    @Schema(description = "Identifier of the owning user", example = "7")
    private Long userId;

    @Schema(description = "Creation timestamp (UTC)", example = "2026-01-15T10:15:30Z")
    private Instant created;

    @Schema(description = "Last modification timestamp (UTC)", example = "2026-02-01T08:05:12Z")
    private Instant modified;
}
