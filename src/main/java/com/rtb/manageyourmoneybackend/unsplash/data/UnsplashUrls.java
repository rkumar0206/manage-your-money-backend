package com.rtb.manageyourmoneybackend.unsplash.data;

public record UnsplashUrls(
        String raw,
        String full,
        String regular, // 1080px width, recommended for standard web display
        String small,
        String thumb
) {}
