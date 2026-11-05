package com.rtb.manageyourmoneybackend.splitexpense.repository;

import com.rtb.manageyourmoneybackend.splitexpense.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroupId(Long groupId);

    boolean existsByPaidByMemberIdOrPaidToMemberId(Long paidByMemberId, Long paidToMemberId);
}
