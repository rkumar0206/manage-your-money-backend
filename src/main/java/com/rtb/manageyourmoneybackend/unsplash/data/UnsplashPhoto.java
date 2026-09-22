package com.rtb.manageyourmoneybackend.unsplash.data;

public record UnsplashPhoto(
        String id,
        String description,
        UnsplashUrls urls
) {}
