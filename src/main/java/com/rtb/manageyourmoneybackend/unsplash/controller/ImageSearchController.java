package com.rtb.manageyourmoneybackend.unsplash.controller;

import com.rtb.manageyourmoneybackend.unsplash.data.UnsplashUrls;
import com.rtb.manageyourmoneybackend.unsplash.service.UnsplashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageSearchController {

    private final UnsplashService unsplashService;

    @GetMapping("/search")
    public ResponseEntity<List<UnsplashUrls>> searchImages(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {

        List<UnsplashUrls> imageUrls = unsplashService.searchImageUrls(keyword, page, limit);
        return ResponseEntity.ok(imageUrls);
    }
}