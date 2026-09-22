package com.rtb.manageyourmoneybackend.unsplash.service;

import com.rtb.manageyourmoneybackend.unsplash.data.UnsplashPhoto;
import com.rtb.manageyourmoneybackend.unsplash.data.UnsplashSearchResponse;
import com.rtb.manageyourmoneybackend.unsplash.data.UnsplashUrls;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class UnsplashService {

    private final RestClient restClient;
    private final String accessKey;

    public UnsplashService(
            RestClient.Builder restClientBuilder,
            @Value("${unsplash.api.base-url:https://api.unsplash.com}") String baseUrl,
            @Value("${unsplash.api.access-key}") String accessKey) {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Accept-Version", "v1") // Required by Unsplash API guidelines
                .build();
        this.accessKey = accessKey;
    }

    public List<UnsplashUrls> searchImageUrls(String keyword) {
        return searchImageUrls(keyword, 1, 10);
    }

    public List<UnsplashUrls> searchImageUrls(String keyword, int page, int perPage) {

        UnsplashSearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/photos")
                        .queryParam("query", keyword)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .build())
                .header("Authorization", "Client-ID " + accessKey)
                .retrieve()
                .body(UnsplashSearchResponse.class);

        if (response == null || response.results() == null) {
            return List.of();
        }

        // Map the photo results directly to the 'regular' size URLs
        return response.results().stream()
                .map(UnsplashPhoto::urls)
                .toList();
    }
}