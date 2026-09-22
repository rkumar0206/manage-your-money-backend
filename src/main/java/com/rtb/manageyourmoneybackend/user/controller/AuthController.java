package com.rtb.manageyourmoneybackend.user.controller;

import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import com.rtb.manageyourmoneybackend.user.service.AppRegistrationHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AppRegistrationHandler appRegistrationHandler;

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam("email") String email) {
        appRegistrationHandler.resendVerificationToken(email);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Registration successful! Please check your inbox to verify your account.",
                        "email", email
                ));
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> activateAccount(@RequestParam("token") String token) {
        return userRepository.findByVerificationToken(token)
                .map(user -> {
                    user.setEnabled(true);
                    user.setVerificationToken(null); // Consume token
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of("message", "Account activated successfully! You may now log in."));
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired activation token.")));
    }
}
