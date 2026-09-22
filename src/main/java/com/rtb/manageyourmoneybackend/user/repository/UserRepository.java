package com.rtb.manageyourmoneybackend.user.repository;

import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Looks up a user by their unique username. Used during the Sign-In authentication flow.
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Verifies if an email is already taken. Used during the Sign-Up registration flow.
     */
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    /**
     * Optional utility if you want to allow users to sign in using either their username OR their email.
     */
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);

    Optional<UserEntity> findByVerificationToken(String token);
}
