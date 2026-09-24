package com.rtb.manageyourmoneybackend.expensecategory.controller;

import com.rksdev.security.web.CurrentUserId;
import com.rtb.manageyourmoneybackend.expensecategory.dto.*;
import com.rtb.manageyourmoneybackend.expensecategory.service.ExpenseCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/expense-categories")
@RequiredArgsConstructor
@Tag(name = "Expense Categories", description = "CRUD operations for user expense categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    @PostMapping
    @Operation(summary = "Create an expense category",
            description = "Creates a new expense category owned by the given user. Idempotent by name: "
                    + "if a category with the same name (case-insensitive) already exists for this user, "
                    + "that existing category is returned (200) instead of creating a duplicate (201).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "200", description = "A category with this name already existed for this user; returned unchanged",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Owning user not found", content = @Content)
    })
    public ResponseEntity<ExpenseCategoryResponseDTO> create(
            @CurrentUserId Long userId,
            @Valid @RequestBody ExpenseCategoryCreateRequestDTO request) {
        ExpenseCategoryCreateResult result = expenseCategoryService.create(userId, request);
        HttpStatus status = result.newlyCreated() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.data());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an expense category by id", description = "Retrieves a single expense category by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    public ResponseEntity<ExpenseCategoryResponseDTO> getById(
            @CurrentUserId Long userId,
            @Parameter(description = "Id of the expense category", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(expenseCategoryService.getById(id, userId));
    }

    @GetMapping
    @Operation(summary = "List expense categories", description = "Returns a paginated list of expense categories for the given user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of categories retrieved successfully")
    })
    public ResponseEntity<Page<ExpenseCategoryResponseDTO>> getAll(@CurrentUserId Long userId, Pageable pageable) {
        return ResponseEntity.ok(expenseCategoryService.getAll(userId, pageable).toPage());
    }

    @GetMapping("/names")
    @Operation(summary = "List expense category names", description = "Returns a list of expense categories for the given user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    public ResponseEntity<CategoryNameResponseDTO> getAllCategoryNames(@CurrentUserId Long userId) {
        return ResponseEntity.ok(expenseCategoryService.getAllCategoryNames(userId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search expense categories by name",
            description = "Case-insensitive, partial-match search over a user's expense categories, paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of matching categories retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or blank search term", content = @Content)
    })
    public ResponseEntity<Page<ExpenseCategoryResponseDTO>> search(
            @CurrentUserId Long userId,
            @Parameter(description = "Search text matched case-insensitively, anywhere in the name", required = true, example = "groc")
            @RequestParam @NotBlank(message = "name must not be blank") String name,
            Pageable pageable) {
        return ResponseEntity.ok(expenseCategoryService.search(userId, name, pageable).toPage());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense category", description = "Updates an existing expense category in place.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully",
                    content = @Content(schema = @Schema(implementation = ExpenseCategoryResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Another Category with same name already present", content = @Content)
    })
    public ResponseEntity<ExpenseCategoryResponseDTO> update(
            @CurrentUserId Long userId,
            @Parameter(description = "Id of the expense category", required = true) @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryUpdateRequestDTO request) {
        return ResponseEntity.ok(expenseCategoryService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense category", description = "Deletes an expense category by its identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    public ResponseEntity<Void> delete(
            @CurrentUserId Long userId,
            @Parameter(description = "Id of the expense category", required = true) @PathVariable Long id) {
        expenseCategoryService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
