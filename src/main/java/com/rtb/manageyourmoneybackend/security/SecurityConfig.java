//package com.rtb.manageyourmoneybackend.security;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.context.SecurityContextHolderStrategy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final FirebaseTokenFilter firebaseTokenFilter;
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // 1. Disable CSRF (since we are stateless and token-based)
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(request -> {
//                    CorsConfiguration config = new CorsConfiguration();
//                    config.setAllowedOrigins(List.of("http://localhost:4200")); // your frontend origin
//                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                    config.setAllowedHeaders(List.of("*"));
//                    config.setAllowCredentials(true);
//                    return config;
//                }))
//                // 2. Set Session Management to STATELESS (no sessions created or used)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                // 3. Define Authorization Rules
//                .authorizeHttpRequests(auth -> auth
//
//                        // Require authentication for all other /api paths
//                        .requestMatchers("/api/**").authenticated()
//
//                        // Reject all other requests by default
//                        .anyRequest().denyAll()
//                )
//
//                // 4. Register our custom Firebase filter
//                // Ensure our token filter runs BEFORE the standard authentication filters
//                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    @Bean
//    public SecurityContextHolderStrategy securityContextHolderStrategy() {
//        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
//        return SecurityContextHolder.getContextHolderStrategy();
//    }
//}
