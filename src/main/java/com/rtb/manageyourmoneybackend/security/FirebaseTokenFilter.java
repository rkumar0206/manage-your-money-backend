package com.rtb.manageyourmoneybackend.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private final FirebaseAuth firebaseAuth;

    private static final String API_PREFIX = "/api/";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Skip if not an API path or if it's an OPTIONS request
        if (!request.getRequestURI().startsWith(API_PREFIX) || request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract Token from Header
        String authToken = request.getHeader("Authorization");

        if (authToken != null && authToken.startsWith("Bearer ")) {
            String idToken = authToken.substring(7);

            try {
                // 3. Verify the ID Token with Firebase Admin SDK
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
                UsernamePasswordAuthenticationToken authentication = getUsernamePasswordAuthenticationToken(decodedToken);

                // 5. Store in Security Context
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (FirebaseAuthException e) {
                // Token is invalid, expired, or revoked
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired Firebase ID token.");
                return;
            }
        } else {
            // Missing Authorization header
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization header required.");
            return;
        }

        // Proceed to the Controller
        filterChain.doFilter(request, response);
    }

    private static UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(FirebaseToken decodedToken) {
        String uid = decodedToken.getUid();

        // 4. Create Authentication Object (Spring Security)
        // Use the UID to create a simple, authenticated token object
        return new UsernamePasswordAuthenticationToken(
                uid, // Principal (the user's Firebase UID)
                null,
                // If you use Spring Security, add authorities here (e.g., "ROLE_USER")
                Collections.emptyList()
        );
    }
}
