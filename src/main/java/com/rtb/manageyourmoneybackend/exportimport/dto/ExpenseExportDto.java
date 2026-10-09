package com.rtb.manageyourmoneybackend.exportimport.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ExpenseExportDto(
        String key,
        String categoryKey,
        String currency,
        String spentOn,
        BigDecimal amount,
        String categoryName,
        List<String> paymentMethods,
        boolean isSynced,
        Instant created,
        Instant modified
) {}
