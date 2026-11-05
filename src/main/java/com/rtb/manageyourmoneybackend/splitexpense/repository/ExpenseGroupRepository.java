package com.rtb.manageyourmoneybackend.splitexpense.repository;

import com.rtb.manageyourmoneybackend.splitexpense.entity.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {
    @Query("SELECT g FROM ExpenseGroup g JOIN GroupMember m ON g.id = m.group.id WHERE m.user.id = :userId")
    List<ExpenseGroup> findAllByUserId(@Param("userId") Long userId);
}
