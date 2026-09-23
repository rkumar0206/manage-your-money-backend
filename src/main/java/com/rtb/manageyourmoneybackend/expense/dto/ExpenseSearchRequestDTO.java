package com.rtb.manageyourmoneybackend.expense.dto;

import com.rtb.manageyourmoneybackend.expense.filter.AmountFilterOperator;
import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * All fields are optional filters — an omitted field simply isn't applied.
 * <p>
 * Date range precedence: if {@code dateRangePreset} is provided and isn't
 * {@code ALL_TIME}, it wins and {@code createdFrom}/{@code createdTo} are
 * ignored. Otherwise the custom {@code createdFrom}/{@code createdTo} pair
 * is used (either side may be left open-ended).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Filter criteria for searching expenses")
@EqualsAndHashCode
public class ExpenseSearchRequestDTO {

    @Schema(description = "Case-insensitive partial match against spentOn", example = "grocery")
    private String spentOn;

    @Schema(description = "Restrict results to a single category")
    private Long categoryId;

    @Schema(description = "Restrict results to expenses using at least one of these payment methods (ANY match)",
            example = "[\"UPI\", \"CREDIT_CARD\"]")
    private List<String> paymentMethods;

    @Schema(description = "How to compare amount against amount/amountTo below")
    private AmountFilterOperator amountOperator;

    @Schema(description = "Primary amount value. Required whenever amountOperator is set.", example = "500.00")
    private BigDecimal amount;

    @Schema(description = "Upper bound; only used (and required) when amountOperator = IS_BETWEEN", example = "1000.00")
    private BigDecimal amountTo;

    @Schema(description = "A predefined date range; takes precedence over createdFrom/createdTo when set and not ALL_TIME")
    private DateRangePreset dateRangePreset;

    @Schema(description = "Custom range start (inclusive), used when dateRangePreset is absent/ALL_TIME")
    private Instant createdFrom;

    @Schema(description = "Custom range end (inclusive), used when dateRangePreset is absent/ALL_TIME")
    private Instant createdTo;

    @AssertTrue(message = "amount is required when amountOperator is set")
    @Schema(hidden = true)
    public boolean isAmountPresentWhenOperatorSet() {
        return amountOperator == null || amount != null;
    }

    @AssertTrue(message = "amountTo is required when amountOperator = IS_BETWEEN")
    @Schema(hidden = true)
    public boolean isAmountToPresentWhenBetween() {
        return amountOperator != AmountFilterOperator.IS_BETWEEN || amountTo != null;
    }
}