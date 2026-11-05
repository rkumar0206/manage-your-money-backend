package com.rtb.manageyourmoneybackend.splitexpense.service;

import com.rtb.manageyourmoneybackend.splitexpense.dtos.*;

import java.util.List;

public interface SplitExpenseService {

    GroupResponse createGroup(Long userId, CreateGroupRequest request);
    List<GroupSummaryResponse> getGroupsForUser(Long userId);
    GroupResponse getGroupDetails(Long userId, Long groupId);
    SplitExpenseResponse addSplitExpense(Long userId, Long groupId, CreateSplitExpenseRequest request);
    List<SplitExpenseResponse> getGroupExpenses(Long userId, Long groupId);
    GroupBalancesResponse getGroupBalances(Long userId, Long groupId);
    SettlementResponse addSettlement(Long userId, Long groupId, CreateSettlementRequest request);
    List<SettlementResponse> getSettlements(Long userId, Long groupId);

    // Group Operations
    void updateGroup(Long userId, Long groupId, UpdateGroupRequest request);
    void deleteGroup(Long userId, Long groupId);
    void removeMember(Long userId, Long groupId, Long memberId);
    void addMembersToGroup(Long userId, Long groupId, AddGroupMembersRequest request);

    // Expense Operations
    SplitExpenseResponse updateExpense(Long userId, Long groupId, Long expenseId, CreateSplitExpenseRequest request);
    void deleteExpense(Long userId, Long groupId, Long expenseId);

    // Settlement Operations
    void deleteSettlement(Long userId, Long groupId, Long settlementId);
}