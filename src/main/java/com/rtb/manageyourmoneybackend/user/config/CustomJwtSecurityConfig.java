package com.rtb.manageyourmoneybackend.user.config;

import com.rksdev.security.api.JwtCustomClaimsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class CustomJwtSecurityConfig {

    @Bean
    public JwtCustomClaimsProvider jwtCustomClaimsProvider() {
        return _ -> {

            Map<String, Object> claims = new HashMap<>();
            claims.put("uuid", UUID.randomUUID().toString());

            return claims;
        };
    }
}
