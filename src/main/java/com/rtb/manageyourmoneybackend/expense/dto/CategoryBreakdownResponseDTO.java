package com.rtb.manageyourmoneybackend.expense.dto;

import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Category-wise breakdown of spend over a date range, sorted by amount descending")
public class CategoryBreakdownResponseDTO {

    @Schema(description = "The date range these totals cover")
    private DateRangePreset dateRangePreset;

    @Schema(description = "Grand total across all categories for this range", example = "2870000.00")
    private BigDecimal totalAmount;

    @Schema(description = "One entry per category that has at least one expense in the range, sorted by amount descending")
    private List<CategoryBreakdownItemDTO> categories;
}
