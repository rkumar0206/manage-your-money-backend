package com.rtb.manageyourmoneybackend.unsplash.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UnsplashResponseDTO {
    private List<UnsplashUrls> urls;
}
