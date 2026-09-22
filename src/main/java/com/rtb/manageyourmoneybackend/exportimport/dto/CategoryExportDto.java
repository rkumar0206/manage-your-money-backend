package com.rtb.manageyourmoneybackend.exportimport.dto;

import java.time.Instant;

public record CategoryExportDto(
        String name,
        String description,
        String imageUrl,
        boolean isSynced,
        Instant created,
        Instant modified
) {}
