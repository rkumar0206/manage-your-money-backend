package com.rtb.manageyourmoneybackend.expensecategory.service;

import com.rtb.manageyourmoneybackend.common.model.PageResponse;
import com.rtb.manageyourmoneybackend.expensecategory.dto.*;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for {@code ExpenseCategory} CRUD operations.
 */
public interface ExpenseCategoryService {

    /**
     * Creates a new expense category owned by the given user.
     *
     * @param userId  id of the {@code @ManyToOne} parent (owning user)
     * @param request validated creation payload
     * @return the persisted category as a response DTO
     */
    ExpenseCategoryCreateResult create(Long userId, ExpenseCategoryCreateRequestDTO request);

    /**
     * Retrieves a single expense category by its id.
     *
     * @throws com.rtb.manageyourmoneybackend.common.exception.ResourceNotFoundException if absent
     */
    ExpenseCategoryResponseDTO getById(Long id, Long userId);

    /**
     * Retrieves all expense categories, optionally filtered to a single owning user.
     *
     * @param userId   optional owning-user filter; {@code null} returns all categories
     * @param pageable pagination and sorting parameters
     */
    PageResponse<ExpenseCategoryResponseDTO> getAll(Long userId, Pageable pageable);

    CategoryNameResponseDTO getAllCategoryNames(Long userId);

    /**
     * Searches a user's expense categories by name, case-insensitively, matching
     * anywhere in the name (not just as a prefix).
     *
     * @param userId   owning user to scope the search to
     * @param name     search text; leading/trailing whitespace is trimmed
     * @param pageable pagination and sorting parameters
     */
    PageResponse<ExpenseCategoryResponseDTO> search(Long userId, String name, Pageable pageable);

    /**
     * Updates an existing expense category in place.
     *
     * @throws com.rtb.manageyourmoneybackend.common.exception.ResourceNotFoundException if absent
     */
    ExpenseCategoryResponseDTO update(Long id, Long userId, ExpenseCategoryUpdateRequestDTO request);

    /**
     * Deletes an expense category by id.
     *
     * @throws com.rtb.manageyourmoneybackend.common.exception.ResourceNotFoundException if absent
     */
    void delete(Long id, Long userId);
}
