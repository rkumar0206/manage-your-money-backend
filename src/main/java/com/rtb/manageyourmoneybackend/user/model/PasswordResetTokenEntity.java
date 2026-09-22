package com.rtb.manageyourmoneybackend.user.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;
}