package com.rtb.manageyourmoneybackend.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Expense details returned to the client")
public class ExpenseResponseDTO {

    @Schema(description = "Unique identifier of the expense", example = "101")
    private Long id;

    @Schema(description = "Free-text note describing what the expense was spent on")
    private String spentOn;

    @Schema(description = "Amount spent", example = "499.99")
    private BigDecimal amount;

    @Schema(description = "Id of the associated expense category", example = "3")
    private Long categoryId;

    // NOTE: assumes ExpenseCategory exposes a "name" property.
    // Adjust/remove this field if that property doesn't exist on your entity.
    @Schema(description = "Name of the associated expense category", example = "Groceries")
    private String categoryName;

    @Schema(description = "Payment methods used for this expense")
    private List<String> paymentMethods;

    @Schema(description = "Whether this expense has been synced", example = "true")
    private boolean synced;

    @Schema(description = "Id of the owning user", example = "42")
    private Long userId;

    @Schema(description = "Timestamp when the expense was incurred")
    private Instant created;

    @Schema(description = "Timestamp when the expense was last modified")
    private Instant modified;
}
