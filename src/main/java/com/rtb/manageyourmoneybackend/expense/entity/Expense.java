package com.rtb.manageyourmoneybackend.expense.entity;

import com.rtb.manageyourmoneybackend.expensecategory.entity.ExpenseCategory;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "expenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spent_on", columnDefinition = "TEXT")
    private String spentOn;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ExpenseCategory category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_methods", columnDefinition = "jsonb")
    private List<String> paymentMethods;

    @Column(name = "is_synced", nullable = false)
    private boolean isSynced;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    //@CreationTimestamp
    @Column(name = "created", nullable = false, updatable = false)
    private Instant created;

    //@UpdateTimestamp
    @Column(name = "modified", nullable = false)
    private Instant modified;
}