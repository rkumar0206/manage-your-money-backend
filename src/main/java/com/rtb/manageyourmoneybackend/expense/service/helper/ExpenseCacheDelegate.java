package com.rtb.manageyourmoneybackend.expense.service.helper;

import com.rtb.manageyourmoneybackend.common.cache.CacheNameConstants;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseResponseDTO;
import com.rtb.manageyourmoneybackend.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Helper service which caches null values to avoid cache penetration
 */
@Component
@RequiredArgsConstructor
public class ExpenseCacheDelegate {

    private final ExpenseRepository expenseRepository;

    @Cacheable(
            cacheNames = CacheNameConstants.EXPENSE_BY_ID,
            key = "#userId + ':' + #id",
            condition = "#userId != null && #id != null"
    )
    public ExpenseResponseDTO findByIdCached(Long userId, Long id) {
        // Returns null if not found, allowing Spring to store 'null' in Redis
        return expenseRepository.findResponseByIdAndUserId(id, userId).orElse(null);
    }
}
