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
@Schema(description = "Top spentOn values by total amount over a date range. NOTE: grouped by exact spentOn text, "
        + "so minor wording/casing differences for what's conceptually the same vendor will appear as separate entries.")
public class TopVendorsResponseDTO {

    @Schema(description = "The date range these totals cover")
    private DateRangePreset dateRangePreset;

    @Schema(description = "Number of entries requested", example = "5")
    private int limit;

    @Schema(description = "Ordered by amount descending, at most `limit` entries")
    private List<VendorAmountDTO> vendors;
}