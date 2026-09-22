package com.rtb.manageyourmoneybackend.user.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class Utils {

    public static String getBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString(); // Returns something like "http://localhost:2266" or "https://api.mydomain.com"
    }
}
