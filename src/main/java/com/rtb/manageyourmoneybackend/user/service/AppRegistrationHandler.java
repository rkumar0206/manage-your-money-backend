package com.rtb.manageyourmoneybackend.user.service;

import com.rtb.manageyourmoneybackend.user.dto.AppSignUpRequest;
import com.rtb.manageyourmoneybackend.user.enums.Roles;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import com.rtb.manageyourmoneybackend.user.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppRegistrationHandler {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public Object handleUserSignUp(AppSignUpRequest request, String encodedPassword) {
        // 1. Enforce unique constraints gracefully
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        // 2. Build and persist the new learner identity
        UserEntity user = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .fullName(request.fullName())
                .password(encodedPassword)
                .roles(Set.of(Roles.REGULAR_USER.getValue()))
                .enabled(false)
                .build();

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        userRepository.save(user);

        String confirmationLink = Utils.getBaseUrl() + "/api/v1/auth/confirm?token=" + token;
        emailService.sendAccountConfirmationEmail(user.getEmail(), confirmationLink);

        return Map.of(
                "message", "Registration successful! Please check your inbox to verify your account.",
                "username", user.getUsername()
        );
    }

    @Transactional
    public void resendVerificationToken(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is not registered.");
        }

        userRepository.findByUsernameOrEmail(email, email).ifPresent(user -> {
            String confirmationLink = Utils.getBaseUrl() + "/api/v1/auth/confirm?token=" + user.getVerificationToken();
            emailService.sendAccountConfirmationEmail(user.getEmail(), confirmationLink);
        });
    }
}