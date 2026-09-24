package com.rtb.manageyourmoneybackend.user.repository;

import com.rtb.manageyourmoneybackend.user.model.RefreshTokenEntity;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    void deleteByUser(UserEntity user);

    // 1. Delete all dead (expired OR revoked) tokens for the user
    @Modifying
    @Transactional
    void deleteByUserIdAndExpiryDateBeforeOrRevokedTrue(Long userId, Instant now);

    // 2. Count active sessions (unrevoked & unexpired)
    long countByUserIdAndRevokedFalseAndExpiryDateAfter(Long userId, Instant now);

    // 3. Find the oldest active session ordered by creation time
    Optional<RefreshTokenEntity> findFirstByUserIdAndRevokedFalseAndExpiryDateAfterOrderByCreatedAtAsc(
            Long userId, Instant now);
}