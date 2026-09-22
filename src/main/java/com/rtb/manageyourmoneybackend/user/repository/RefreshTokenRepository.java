package com.rtb.manageyourmoneybackend.user.repository;

import com.rtb.manageyourmoneybackend.user.model.RefreshTokenEntity;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    void deleteByUser(UserEntity user);
}