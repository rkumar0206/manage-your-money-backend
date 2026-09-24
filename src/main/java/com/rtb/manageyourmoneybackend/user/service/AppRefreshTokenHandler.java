package com.rtb.manageyourmoneybackend.user.service;

import com.rksdev.security.api.PluggableRefreshTokenHandler;
import com.rtb.manageyourmoneybackend.user.model.RefreshTokenEntity;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import com.rtb.manageyourmoneybackend.user.repository.RefreshTokenRepository;
import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.security.max-active-sessions:5}")
    private Integer maxActiveSessions;

    @Override
    @Transactional
    public void saveRefreshToken(String username, String token, Instant expiryDate) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found matching identity: " + username));

        Instant now = Instant.now();

        // 1. Housekeeping: Remove expired or previously revoked tokens for this user
        refreshTokenRepository.deleteByUserIdAndExpiryDateBeforeOrRevokedTrue(user.getId(), now);

        // 2. Count active (unexpired & unrevoked) sessions
        long currentActiveSessions = refreshTokenRepository
                .countByUserIdAndRevokedFalseAndExpiryDateAfter(user.getId(), now);

        // 3. Evict oldest active session(s) if max session limit is reached
        while (currentActiveSessions >= maxActiveSessions) {
            Optional<RefreshTokenEntity> oldestSession = refreshTokenRepository
                    .findFirstByUserIdAndRevokedFalseAndExpiryDateAfterOrderByCreatedAtAsc(user.getId(), now);

            if (oldestSession.isPresent()) {
                refreshTokenRepository.delete(oldestSession.get());
                currentActiveSessions--;
            } else {
                break;
            }
        }

        // 4. Save the new refresh token
        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        refreshTokenRepository.save(tokenEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getUsernameIfValid(String token) {
        Instant now = Instant.now();
        return refreshTokenRepository.findByToken(token)
                .filter(tokenEntity -> !tokenEntity.isRevoked())                // Must not be revoked
                .filter(tokenEntity -> tokenEntity.getExpiryDate().isAfter(now)) // Must not be expired
                .map(tokenEntity -> tokenEntity.getUser().getUsername());
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(tokenEntity -> {
                    tokenEntity.setRevoked(true);
                    refreshTokenRepository.save(tokenEntity);
                });
    }
}