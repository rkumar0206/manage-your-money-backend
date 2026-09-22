package com.rtb.manageyourmoneybackend.expense.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Payload used to create a new {@code Expense}.
 * <p>
 * {@code user} is intentionally excluded — the owning user is resolved
 * server-side from {@code @CurrentUserId}. {@code isSynced} is always
 * forced to {@code true} by the service layer and is not client-settable.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Payload for creating a new expense")
public class ExpenseCreateRequestDTO {

    @Schema(description = "Free-text note describing what the expense was spent on",
            example = "Grocery shopping at BigBasket")
    private String spentOn;

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.0", inclusive = true, message = "Amount cannot be negative")
    @Schema(description = "Amount spent; zero or positive only", example = "499.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotNull(message = "Category id is mandatory")
    @Schema(description = "Id of the expense category this expense belongs to", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @Schema(description = "Payment methods used for this expense", example = "[\"UPI\", \"CREDIT_CARD\"]")
    private List<String> paymentMethods;

    @Schema(description = "When the expense was actually incurred. If omitted, the server defaults it to the current time",
            example = "2025-01-15T10:30:00Z")
    private Instant created;
}
