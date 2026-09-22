package com.rtb.manageyourmoneybackend.unsplash.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record UnsplashSearchResponse(
        int total,
        @JsonProperty("total_pages") int totalPages,
        List<UnsplashPhoto> results
) {}

