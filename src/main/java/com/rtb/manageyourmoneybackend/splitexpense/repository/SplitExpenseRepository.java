package com.rtb.manageyourmoneybackend.splitexpense.repository;

import com.rtb.manageyourmoneybackend.splitexpense.entity.SplitExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SplitExpenseRepository extends JpaRepository<SplitExpense, Long> {
    List<SplitExpense> findByGroupIdOrderByExpenseDateDesc(Long groupId);
    boolean existsByPaidByMemberId(Long memberId);
}
