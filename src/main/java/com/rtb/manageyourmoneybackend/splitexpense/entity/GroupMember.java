package com.rtb.manageyourmoneybackend.splitexpense.entity;

import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "group_members",
        uniqueConstraints = {
                // Ensures a registered user isn't added to the same group twice
                @UniqueConstraint(columnNames = {"group_id", "user_id"}, name = "uq_group_members_group_user")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private ExpenseGroup group;

    // Nullable: Allows dummy users who haven't registered yet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // Required: Display name (e.g., "Rohit" or "Alice (Guest)")
    @Column(nullable = false)
    private String name;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private UserEntity createdBy;
}