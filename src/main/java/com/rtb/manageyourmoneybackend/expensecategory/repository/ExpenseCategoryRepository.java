package com.rtb.manageyourmoneybackend.expensecategory.repository;

import com.rtb.manageyourmoneybackend.expensecategory.entity.ExpenseCategory;
import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    @QueryHints(value = {
            @QueryHint(name = HibernateHints.HINT_FETCH_SIZE, value = "500"),
            @QueryHint(name = HibernateHints.HINT_READ_ONLY, value = "true")
    })
    @Query("SELECT c FROM ExpenseCategory c WHERE c.user.id = :userId")
    Stream<ExpenseCategory> streamByUserId(Long userId);

    Optional<ExpenseCategory> findByUserIdAndName(Long userId, String name);


    /**
     * Returns a page of expense categories scoped to a single owning user —
     * used for the "list categories for user X" filtering scenario.
     */
    @Query("SELECT ec FROM ExpenseCategory ec where ec.user.id = :userId")
    Page<ExpenseCategory> findAllByUser_Id(Long userId, Pageable pageable);

    /**
     * Fetches a single category, scoped to its owning user, useful for
     * ownership-checked lookups (e.g. "does category X belong to user Y").
     */
    Optional<ExpenseCategory> findByIdAndUser_Id(Long id, Long userId);

    boolean existsByIdAndUser_Id(Long id, Long userId);

    /**
     * Case-insensitive lookup of a category by name, scoped to its owning user.
     * Backs the idempotent-create flow: a repeat create call with the same
     * (case-insensitive) name for the same user returns the existing row
     * instead of inserting a duplicate.
     */
    Optional<ExpenseCategory> findByUser_IdAndNameIgnoreCase(Long userId, String name);

    /**
     * Used on update to reject renaming a category to a name that collides
     * (case-insensitively) with another category already owned by the same user.
     */
    boolean existsByUser_IdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);

    /**
     * Case-insensitive, partial-match ("contains") search over a user's categories,
     * paginated. Backs the /search endpoint.
     */
    Page<ExpenseCategory> findByUser_IdAndNameContainingIgnoreCase(Long userId, String name, Pageable pageable);

}
