package com.rtb.manageyourmoneybackend.expense.dto;

import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Spend broken down by payment method, sorted by amount descending. NOTE: if an expense "
        + "lists more than one payment method, its full amount is counted once per method listed (not split "
        + "between them), matching what the source data represents — a set of methods that were involved, not a "
        + "per-method split of the total.")
public class PaymentMethodDistributionResponseDTO {

    @Schema(description = "The date range these totals cover")
    private DateRangePreset dateRangePreset;

    @Schema(description = "One entry per distinct payment method that appears at least once in the range, sorted by amount descending")
    private List<PaymentMethodAmountDTO> paymentMethods;
}