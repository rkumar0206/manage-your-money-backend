package com.rtb.manageyourmoneybackend.expensecategory.service.impl;

import com.rtb.manageyourmoneybackend.common.cache.CacheEvictionService;
import com.rtb.manageyourmoneybackend.common.cache.CacheNameConstants;
import com.rtb.manageyourmoneybackend.common.exception.DuplicateResourceException;
import com.rtb.manageyourmoneybackend.common.exception.ResourceNotFoundException;
import com.rtb.manageyourmoneybackend.common.model.PageResponse;
import com.rtb.manageyourmoneybackend.common.util.CommonAppUtil;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryExpenseSummary;
import com.rtb.manageyourmoneybackend.expense.repository.ExpenseRepository;
import com.rtb.manageyourmoneybackend.expensecategory.adapter.ExpenseCategoryAdapter;
import com.rtb.manageyourmoneybackend.expensecategory.dto.*;
import com.rtb.manageyourmoneybackend.expensecategory.entity.ExpenseCategory;
import com.rtb.manageyourmoneybackend.expensecategory.mapper.ExpenseCategoryMapper;
import com.rtb.manageyourmoneybackend.expensecategory.repository.ExpenseCategoryRepository;
import com.rtb.manageyourmoneybackend.expensecategory.service.ExpenseCategoryService;
import com.rtb.manageyourmoneybackend.firebase.models.CategoryItem;
import com.rtb.manageyourmoneybackend.firebase.sevice.FirestoreService;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import com.rtb.manageyourmoneybackend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Default {@link ExpenseCategoryService} implementation.
 * <p>
 * NOTE: this class assumes a {@code UserRepository} exists at
 * {@code com.rtb.manageyourmoneybackend.user.repository.UserRepository} exposing
 * the standard {@code findById(Long)}. Adjust the import/package if the actual
 * location in your codebase differs.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {

    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final UserRepository userRepository;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final ExpenseRepository expenseRepository;
    private final CacheEvictionService cacheEvictionService;
    private final FirestoreService firestoreService;
    private final UserService userService;

    private static final String[] CATEGORY_USER_CACHES = {
            CacheNameConstants.EXPENSE_CATEGORY_BY_ID,
            CacheNameConstants.EXPENSE_CATEGORY_LIST
    };

    @Override
    @Transactional
    public ExpenseCategoryCreateResult create(Long userId, ExpenseCategoryCreateRequestDTO request) {

        CommonAppUtil.validateIdField(userId, "userId");

        String normalizedName = normalizeName(request.getName());

        // Idempotency check: a category with this name (case-insensitive) already
        // exists for this user -> return it as-is instead of inserting a duplicate.
        Optional<ExpenseCategory> existing = expenseCategoryRepository.findByUser_IdAndNameIgnoreCase(userId, normalizedName);
        if (existing.isPresent()) {
            return new ExpenseCategoryCreateResult(expenseCategoryMapper.toResponseDto(existing.get()), false);
        }

        UserEntity owner = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        ExpenseCategory entity = expenseCategoryMapper.toEntity(request);

        entity.setName(normalizedName);
        entity.setSynced(false);
        entity.setKey(UUID.randomUUID().toString());

        if (entity.getCreated() == null) {
            entity.setCreated(Instant.now());
        }

        if (entity.getModified() == null) {
            entity.setModified(Instant.now());
        }

        entity.setUser(owner);

        try {
            ExpenseCategory saved = expenseCategoryRepository.save(entity);

            uploadToFirestore(userId, saved, false);
            ExpenseCategoryResponseDTO responseDto = expenseCategoryMapper.toResponseDto(saved);
            responseDto.setTotalExpenseAmount(BigDecimal.valueOf(0.0));
            evictUserCaches(userId);
            return new ExpenseCategoryCreateResult(responseDto, true);
        } catch (DataIntegrityViolationException ex) {
            // Two concurrent requests raced past the check above; if the DB enforces
            // a unique (user_id, lower(name)) constraint, fall back to the row the
            // other transaction just committed instead of surfacing a 500.
            return expenseCategoryRepository.findByUser_IdAndNameIgnoreCase(userId, normalizedName)
                    .map(row -> {
                        ExpenseCategoryResponseDTO responseDto = expenseCategoryMapper.toResponseDto(row);
                        responseDto.setTotalExpenseAmount(expenseRepository.sumAmountByUserIdAndCategoryId(userId, responseDto.getId()));
                        return new ExpenseCategoryCreateResult(responseDto, false);
                    })
                    .orElseThrow(() -> ex);
        }
    }

    @Override
    @Cacheable(
            cacheNames = CacheNameConstants.EXPENSE_CATEGORY_BY_ID,
            key = "#userId + ':' + #id",
            condition = "#userId != null && #id != null"
    )
    public ExpenseCategoryResponseDTO getById(Long id, Long userId) {
        CommonAppUtil.validateIdField(userId, "userId");
        CommonAppUtil.validateIdField(id, "id");
        ExpenseCategory entity = findEntityOrThrow(id, userId);
        ExpenseCategoryResponseDTO responseDto = expenseCategoryMapper.toResponseDto(entity);
        responseDto.setTotalExpenseAmount(expenseRepository.sumAmountByUserIdAndCategoryId(userId, id));
        return responseDto;
    }

    @Override
    @Cacheable(
            cacheNames = CacheNameConstants.EXPENSE_CATEGORY_LIST,
            key = "#userId + ':page:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public PageResponse<ExpenseCategoryResponseDTO> getAll(Long userId, Pageable pageable) {
        CommonAppUtil.validateIdField(userId, "userId");

        Page<ExpenseCategory> page = expenseCategoryRepository.findAllByUser_Id(userId, pageable);

        Page<ExpenseCategoryResponseDTO> responses = page.map(expenseCategoryMapper::toResponseDto);
        addTotalSumAmountToTheCategories(userId, responses);
        return PageResponse.fromPage(responses);
    }

    @Override
    public CategoryNameResponseDTO getAllCategoryNames(Long userId) {
        CommonAppUtil.validateIdField(userId, "userId");
        return new CategoryNameResponseDTO(expenseCategoryRepository.findAllCategoryNamesByUserId(userId));
    }

    @Override
    @Cacheable(
            cacheNames = CacheNameConstants.EXPENSE_CATEGORY_LIST,
            key = "#userId + ':search:name=' + (#name == null ? '' : #name) "
                    + "+ ':page:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public PageResponse<ExpenseCategoryResponseDTO> search(Long userId, String name, Pageable pageable) {

        CommonAppUtil.validateIdField(userId, "userId");

        String query = normalizeName(name);
        Page<ExpenseCategoryResponseDTO> responses = expenseCategoryRepository.findByUser_IdAndNameContainingIgnoreCase(userId, query, pageable)
                .map(expenseCategoryMapper::toResponseDto);

        addTotalSumAmountToTheCategories(userId, responses);

        return PageResponse.fromPage(responses);
    }


    @Override
    @Transactional
    @CachePut(value = "expense_category", key = "#userId + ':' + #id")
    public ExpenseCategoryResponseDTO update(Long id, Long userId, ExpenseCategoryUpdateRequestDTO request) {

        CommonAppUtil.validateIdField(userId, "userId");
        CommonAppUtil.validateIdField(id, "id");

        ExpenseCategory entity = findEntityOrThrow(id, userId);

        String normalizedName = normalizeName(request.getName());

        if (expenseCategoryRepository.existsByUser_IdAndNameIgnoreCaseAndIdNot(userId, normalizedName, id)) {
            throw new DuplicateResourceException(
                    "Another expense category named '" + normalizedName + "' already exists for this user");
        }

        expenseCategoryMapper.updateEntityFromDto(request, entity);

        entity.setName(normalizedName);
        entity.setSynced(true);

        if (entity.getCreated() == null) {
            entity.setCreated(Instant.now());
        }

        entity.setModified(Instant.now());

        ExpenseCategory saved = expenseCategoryRepository.save(entity);

        uploadToFirestore(userId, saved, true);

        ExpenseCategoryResponseDTO responseDto = expenseCategoryMapper.toResponseDto(saved);
        responseDto.setTotalExpenseAmount(expenseRepository.sumAmountByUserIdAndCategoryId(userId, responseDto.getId()));
        evictUserCaches(userId);
        return responseDto;
    }

    @Override
    @Transactional
    @CacheEvict(value = "expense_category", key = "#userId + ':' + #id")
    public void delete(Long id, Long userId) {
        CommonAppUtil.validateIdField(userId, "userId");
        CommonAppUtil.validateIdField(id, "id");

        Optional<ExpenseCategory> byIdAndUserId = expenseCategoryRepository.findByIdAndUser_Id(id, userId);
        if (byIdAndUserId.isEmpty()) {
            throw ResourceNotFoundException.of("ExpenseCategory", id);
        }
        expenseCategoryRepository.deleteById(id);
        deleteFromFirestore(byIdAndUserId.get().getKey());
        evictUserCaches(userId);
    }

    private void uploadToFirestore(Long userId, ExpenseCategory expenseCategory, boolean update) {

        // save to firestore
        try {
            String email = userService.getCurrentUserDetails(userId).email();

            String uid = email.equalsIgnoreCase("rkumar0206mnirks@gmail.com")
                    ? "IgkfuqhwpZh2CIXjdqZKxzxbe4j1"
                    : firestoreService.getUid(email);

            if (!StringUtils.hasText(uid)) {
                return;
            }

            firestoreService.save(ExpenseCategoryAdapter.toCategoryItem(uid, expenseCategory), update);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Unable to upload to firestore", e);
        }
    }

    private void deleteFromFirestore(String key) {

        // save to firestore
        try {
            firestoreService.delete(CategoryItem.class, key);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Unable to delete item from firestore", e);
        }
    }

    private ExpenseCategory findEntityOrThrow(Long id, Long userId) {
        return expenseCategoryRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("ExpenseCategory", id));
    }

    /**
     * Trims incidental whitespace so that e.g. {@code " Groceries"} and
     * {@code "Groceries"} are treated as the same name for uniqueness purposes.
     * Blank-check is already covered by {@code @NotBlank} on the DTOs.
     */
    private String normalizeName(String name) {
        return name.trim();
    }

    private void addTotalSumAmountToTheCategories(Long userId, Page<ExpenseCategoryResponseDTO> responses) {
        Map<Long, BigDecimal> amountByCategoryId = getAmountGroupedByCategoryIds(userId);

        for (ExpenseCategoryResponseDTO responseDto : responses) {
            responseDto.setTotalExpenseAmount(amountByCategoryId.get(responseDto.getId()));
        }
    }

    private Map<Long, BigDecimal> getAmountGroupedByCategoryIds(Long userId) {

        List<CategoryExpenseSummary> categoryExpenseSummaries = expenseRepository.sumAmountByUserIdGroupedByCategory(userId);
        return categoryExpenseSummaries.stream()
                .collect(Collectors.toMap(CategoryExpenseSummary::categoryId, CategoryExpenseSummary::totalAmount));
    }

    private void evictUserCaches(Long userId) {
        try {
            cacheEvictionService.evictByUser(userId, CATEGORY_USER_CACHES);
        } catch (Exception ex) {
            log.warn("Category cache eviction failed for userId={}: {}", userId, ex.getMessage());
        }
    }
}
