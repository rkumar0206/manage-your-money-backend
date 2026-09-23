package com.rtb.manageyourmoneybackend.expense.controller;

import com.rksdev.security.web.CurrentUserId;
import com.rtb.manageyourmoneybackend.expense.dto.*;
import com.rtb.manageyourmoneybackend.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Endpoints for managing the current user's expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create a new expense")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Expense created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<ExpenseResponseDTO> create(
            @CurrentUserId Long userId,
            @Valid @RequestBody ExpenseCreateRequestDTO request) {
        ExpenseResponseDTO response = expenseService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an expense by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense found"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<ExpenseResponseDTO> getById(
            @CurrentUserId Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(userId, id));
    }

    @GetMapping
    @Operation(summary = "Get all expenses for the current user, paginated")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<Page<ExpenseResponseDTO>> getAll(
            @CurrentUserId Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(expenseService.getAll(userId, pageable).toPage());
    }

    @GetMapping("/by-category/{categoryId}")
    @Operation(summary = "Get all expenses for the current user filtered by category, paginated")
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    public ResponseEntity<Page<ExpenseResponseDTO>> getAllByUserIdAndCategoryId(
            @CurrentUserId Long userId,
            @Parameter(description = "Id of the category to filter expenses by", required = true)
            @PathVariable Long categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(expenseService.getAllByUserIdAndCategoryId(userId, categoryId, pageable).toPage());
    }

    @GetMapping("/total")
    @Operation(summary = "Get the total amount spent by the current user across all categories")
    @ApiResponse(responseCode = "200", description = "Total amount retrieved successfully")
    public ResponseEntity<BigDecimal> getTotalAmountSpentByUserId(
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(expenseService.getTotalAmountSpentByUserId(userId));
    }

    @GetMapping("/total/by-category/{categoryId}")
    @Operation(summary = "Get the total amount spent by the current user within a specific category")
    @ApiResponse(responseCode = "200", description = "Total amount retrieved successfully")
    public ResponseEntity<BigDecimal> getTotalAmountSpentByCategoryId(
            @CurrentUserId Long userId,
            @Parameter(description = "Id of the category to total expenses for", required = true)
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(expenseService.getTotalAmountSpentByCategoryId(userId, categoryId));
    }


    @PostMapping("/search")
    @Operation(summary = "Search the current user's expenses with dynamic filters, paginated",
            description = "Filter by spentOn (case-insensitive contains), amount (equals/less-than/greater-than/between), "
                    + "a predefined or custom created-date range, paymentMethods (any match), and/or categoryId. "
                    + "Every field in the request body is optional.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter combination (e.g. IS_BETWEEN without amountTo)")
    })
    public ResponseEntity<Page<ExpenseResponseDTO>> search(
            @CurrentUserId Long userId,
            @Valid @RequestBody ExpenseSearchRequestDTO criteria,
            Pageable pageable) {
        return ResponseEntity.ok(expenseService.search(userId, criteria, pageable).toPage());
    }

    @PostMapping("/search/total")
    @Operation(summary = "Get the total amount for the same filters accepted by /search")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total amount retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter combination (e.g. IS_BETWEEN without amountTo)")
    })
    public ResponseEntity<BigDecimal> searchTotal(
            @CurrentUserId Long userId,
            @Valid @RequestBody ExpenseSearchRequestDTO criteria) {
        return ResponseEntity.ok(expenseService.getTotalAmountSpentBySearchCriteria(userId, criteria));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing expense")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Expense updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Expense or category not found")
    })
    public ResponseEntity<ExpenseResponseDTO> update(
            @CurrentUserId Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseUpdateRequestDTO request) {
        return ResponseEntity.ok(expenseService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Expense deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Void> delete(@CurrentUserId Long userId, @PathVariable Long id) {
        expenseService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/payment-methods")
    @Operation(summary = "Get the distinct payment methods used across the current user's expenses")
    @ApiResponse(responseCode = "200", description = "Payment methods retrieved successfully")
    public ResponseEntity<PaymentMethodsResponse> getDistinctPaymentMethods(@CurrentUserId Long userId) {
        return ResponseEntity.ok(expenseService.getDistinctPaymentMethods(userId));
    }
}
