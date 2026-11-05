package com.rtb.manageyourmoneybackend.splitexpense.controller;

import com.rksdev.security.web.CurrentUserId;
import com.rtb.manageyourmoneybackend.splitexpense.dtos.*;
import com.rtb.manageyourmoneybackend.splitexpense.service.SplitExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/split-expenses")
@RequiredArgsConstructor
@Tag(name = "Split Expenses", description = "Endpoints for managing shared group expenses (Splitwise)")
public class SplitExpenseController {

    private final SplitExpenseService service;

    // ==================================================================================
    // GROUP MANAGEMENT
    // ==================================================================================

    @PostMapping("/groups")
    @Operation(summary = "Create a new split group", description = "Creates a group and adds the provided members (registered or dummy). The creator is automatically added if not included.")
    public ResponseEntity<GroupResponse> createGroup(
            @CurrentUserId Long userId,
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createGroup(userId, request));
    }

    @GetMapping("/groups")
    @Operation(summary = "Get all groups for the current user", description = "Returns a summary of all groups the user is a member of, including their net balance in each group.")
    public ResponseEntity<List<GroupSummaryResponse>> getGroups(
            @CurrentUserId Long userId) {
        return ResponseEntity.ok(service.getGroupsForUser(userId));
    }

    @GetMapping("/groups/{groupId}")
    @Operation(summary = "Get group details", description = "Returns the group metadata, list of members, and creator info.")
    public ResponseEntity<GroupResponse> getGroupDetails(
            @CurrentUserId Long userId,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(service.getGroupDetails(userId, groupId));
    }

    // ==================================================================================
    // EXPENSES
    // ==================================================================================

    @PostMapping("/groups/{groupId}/expenses")
    @Operation(summary = "Add a split expense", description = "Logs a new expense in the group and calculates the shares based on EQUAL, EXACT, or PERCENTAGE split type.")
    public ResponseEntity<SplitExpenseResponse> addExpense(
            @CurrentUserId Long userId,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateSplitExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addSplitExpense(userId, groupId, request));
    }

    @GetMapping("/groups/{groupId}/expenses")
    @Operation(summary = "Get all group expenses", description = "Returns a chronological list of all expenses logged in the group, along with who paid and how it was split.")
    public ResponseEntity<List<SplitExpenseResponse>> getExpenses(
            @CurrentUserId Long userId,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(service.getGroupExpenses(userId, groupId));
    }

    // ==================================================================================
    // BALANCES (MATH ENGINE)
    // ==================================================================================

    @GetMapping("/groups/{groupId}/balances")
    @Operation(summary = "Calculate group balances", description = "Returns the net balance for every member and a simplified list of transactions required to settle all debts.")
    public ResponseEntity<GroupBalancesResponse> getBalances(
            @CurrentUserId Long userId,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(service.getGroupBalances(userId, groupId));
    }

    // ==================================================================================
    // SETTLEMENTS
    // ==================================================================================

    @PostMapping("/groups/{groupId}/settlements")
    @Operation(summary = "Record a cash settlement", description = "Records a payment made between two members outside the app (e.g., via UPI or cash) to settle a debt.")
    public ResponseEntity<SettlementResponse> addSettlement(
            @CurrentUserId Long userId,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateSettlementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addSettlement(userId, groupId, request));
    }

    @GetMapping("/groups/{groupId}/settlements")
    @Operation(summary = "Get settlement history", description = "Returns a list of all cash settlements recorded in the group.")
    public ResponseEntity<List<SettlementResponse>> getSettlements(
            @CurrentUserId Long userId,
            @PathVariable Long groupId) {
        return ResponseEntity.ok(service.getSettlements(userId, groupId));
    }

    // --- GROUP MANAGEMENT ---
    @PutMapping("/groups/{groupId}")
    @Operation(summary = "Update group details")
    public ResponseEntity<GroupResponse> updateGroup(
            @CurrentUserId Long userId,
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request) {
        service.updateGroup(userId, groupId, request);
        return ResponseEntity.ok(service.getGroupDetails(userId, groupId)); // Return updated group
    }

    @PostMapping("/groups/{groupId}/members")
    @Operation(summary = "Add members to an existing group")
    public ResponseEntity<Void> addMembers(
            @CurrentUserId Long userId,
            @PathVariable Long groupId,
            @Valid @RequestBody AddGroupMembersRequest request) {
        service.addMembersToGroup(userId, groupId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/groups/{groupId}")
    @Operation(summary = "Delete a group and all its data")
    public ResponseEntity<Void> deleteGroup(
            @CurrentUserId Long userId,
            @PathVariable Long groupId) {
        service.deleteGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{groupId}/members/{memberId}")
    @Operation(summary = "Remove a member from a group")
    public ResponseEntity<Void> removeMember(
            @CurrentUserId Long userId,
            @PathVariable Long groupId,
            @PathVariable Long memberId) {
        service.removeMember(userId, groupId, memberId);
        return ResponseEntity.noContent().build();
    }

    // --- EXPENSE MANAGEMENT ---
    @PutMapping("/groups/{groupId}/expenses/{expenseId}")
    @Operation(summary = "Update an existing expense")
    public ResponseEntity<SplitExpenseResponse> updateExpense(
            @CurrentUserId Long userId,
            @PathVariable Long groupId,
            @PathVariable Long expenseId,
            @Valid @RequestBody CreateSplitExpenseRequest request) {
        return ResponseEntity.ok(service.updateExpense(userId, groupId, expenseId, request));
    }

    @DeleteMapping("/groups/{groupId}/expenses/{expenseId}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<Void> deleteExpense(
            @CurrentUserId Long userId,
            @PathVariable Long groupId,
            @PathVariable Long expenseId) {
        service.deleteExpense(userId, groupId, expenseId);
        return ResponseEntity.noContent().build();
    }

    // --- SETTLEMENT MANAGEMENT ---
    @DeleteMapping("/groups/{groupId}/settlements/{settlementId}")
    @Operation(summary = "Delete a settlement")
    public ResponseEntity<Void> deleteSettlement(
            @CurrentUserId Long userId,
            @PathVariable Long groupId,
            @PathVariable Long settlementId) {
        service.deleteSettlement(userId, groupId, settlementId);
        return ResponseEntity.noContent().build();
    }
}