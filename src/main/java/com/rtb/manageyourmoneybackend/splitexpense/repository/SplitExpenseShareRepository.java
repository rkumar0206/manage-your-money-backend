package com.rtb.manageyourmoneybackend.splitexpense.repository;

import com.rtb.manageyourmoneybackend.splitexpense.entity.SplitExpenseShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SplitExpenseShareRepository extends JpaRepository<SplitExpenseShare, Long> {
    List<SplitExpenseShare> findBySplitExpenseId(Long splitExpenseId);

    @Query("SELECT s FROM SplitExpenseShare s JOIN SplitExpense e ON s.splitExpense.id = e.id WHERE e.group.id = :groupId")
    List<SplitExpenseShare> findAllByGroupId(@Param("groupId") Long groupId);
    boolean existsByMemberId(Long memberId);

    @Modifying
    @Query("DELETE FROM SplitExpenseShare s WHERE s.splitExpense.id = :expenseId")
    void deleteAllBySplitExpenseId(@Param("expenseId") Long expenseId);
}
