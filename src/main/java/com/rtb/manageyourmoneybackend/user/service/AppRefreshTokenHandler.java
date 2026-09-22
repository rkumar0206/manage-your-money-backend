package com.rtb.manageyourmoneybackend.user.service;

import com.rksdev.security.api.PluggableRefreshTokenHandler;
import com.rtb.manageyourmoneybackend.user.model.RefreshTokenEntity;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import com.rtb.manageyourmoneybackend.user.repository.RefreshTokenRepository;
import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppRefreshTokenHandler implements PluggableRefreshTokenHandler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void saveRefreshToken(String username, String token, Instant expiryDate) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found matching identity: " + username));

        // Enforce a single active refresh token session per user by clearing old ones
        refreshTokenRepository.deleteByUser(user);

        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .build();

        refreshTokenRepository.save(tokenEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getUsernameIfValid(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(tokenEntity -> tokenEntity.getExpiryDate().isAfter(Instant.now())) // Must not be expired
                .map(tokenEntity -> tokenEntity.getUser().getUsername());
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshTokenRepository::delete);
    }
}
