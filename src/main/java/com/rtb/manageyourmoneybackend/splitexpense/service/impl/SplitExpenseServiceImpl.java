package com.rtb.manageyourmoneybackend.splitexpense.service.impl;

import com.rtb.manageyourmoneybackend.splitexpense.dtos.*;
import com.rtb.manageyourmoneybackend.splitexpense.entity.*;
import com.rtb.manageyourmoneybackend.splitexpense.repository.*;
import com.rtb.manageyourmoneybackend.splitexpense.service.SplitExpenseService;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SplitExpenseServiceImpl implements SplitExpenseService {

    private final ExpenseGroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;
    private final SplitExpenseRepository expenseRepository;
    private final SplitExpenseShareRepository shareRepository;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;

    // ==================================================================================
    // GROUPS
    // ==================================================================================

    @Override
    @Transactional
    public GroupResponse createGroup(Long userId, CreateGroupRequest request) {
        UserEntity creator = getUserOrThrow(userId);

        ExpenseGroup group = ExpenseGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(creator)
                .created(Instant.now())
                .modified(Instant.now())
                .createdBy(creator)
                .build();
        group = groupRepository.save(group);

        List<GroupMember> savedMembers = new ArrayList<>();
        boolean creatorFoundInRequest = false;

        // 1. Process all members from the request (registered or dummy)
        for (GroupMemberInput input : request.getMembers()) {
            UserEntity linkedUser = input.getUserId() != null ? getUserOrThrow(input.getUserId()) : null;

            // If the input matches the creator's ID, mark it so we don't duplicate them later
            if (linkedUser != null && linkedUser.getId().equals(userId)) {
                creatorFoundInRequest = true;
            }

            GroupMember member = GroupMember.builder()
                    .group(group)
                    .user(linkedUser)
                    .name(input.getName())
                    .joinedAt(Instant.now())
                    .createdBy(creator)
                    .build();
            savedMembers.add(memberRepository.save(member));
        }

        // 2. Safety net: Ensure the creator is always a member of their own group
        if (!creatorFoundInRequest) {
            GroupMember creatorMember = GroupMember.builder()
                    .group(group)
                    .user(creator)
                    .name(creator.getFullName())
                    .joinedAt(Instant.now())
                    .createdBy(creator)
                    .build();
            savedMembers.add(memberRepository.save(creatorMember));
        }

        log.info("Group {} created by user {}", group.getId(), userId);
        return mapToGroupResponse(group, savedMembers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupSummaryResponse> getGroupsForUser(Long userId) {
        // Fetch groups where the user is the creator OR a registered member
        List<ExpenseGroup> groups = groupRepository.findAllByUserId(userId);

        return groups.stream().map(g -> {
            List<GroupMember> members = memberRepository.findByGroupId(g.getId());
            List<SplitExpense> expenses = expenseRepository.findByGroupIdOrderByExpenseDateDesc(g.getId());

            BigDecimal totalSpent = expenses.stream()
                    .map(SplitExpense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Find the member entity corresponding to the logged-in user
            GroupMember userMember = members.stream()
                    .filter(m -> m.getUser() != null && m.getUser().getId().equals(userId))
                    .findFirst().orElse(null);

            BigDecimal userNetBalance = BigDecimal.ZERO;
            if (userMember != null) {
                userNetBalance = computeUserNetBalanceOptimized(g.getId(), userMember.getId(), expenses);
            }

            return GroupSummaryResponse.builder()
                    .id(g.getId())
                    .name(g.getName())
                    .description(g.getDescription())
                    .memberCount(members.size())
                    .totalSpent(totalSpent)
                    .userNetBalance(userNetBalance)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupDetails(Long userId, Long groupId) {
        verifyMembership(userId, groupId);
        ExpenseGroup group = getGroupOrThrow(groupId);
        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        return mapToGroupResponse(group, members);
    }

    // ==================================================================================
    // EXPENSES
    // ==================================================================================

    @Override
    @Transactional
    public SplitExpenseResponse addSplitExpense(Long userId, Long groupId, CreateSplitExpenseRequest request) {
        verifyMembership(userId, groupId);

        UserEntity currentUser = getUserOrThrow(userId);
        ExpenseGroup group = getGroupOrThrow(groupId);
        GroupMember paidByMember = getMemberOrThrow(request.getPaidByMemberId());

        if (!paidByMember.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Payer must be a member of this group");
        }

        SplitExpense expense = SplitExpense.builder()
                .group(group)
                .paidByMember(paidByMember)
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .created(Instant.now())
                .modified(Instant.now())
                .createdBy(currentUser)
                .build();
        expense = expenseRepository.save(expense);

        List<SplitExpenseShare> shares = calculateShares(expense, groupId, request, currentUser);
        validateSharesSum(request.getAmount(), shares);
        shares = shareRepository.saveAll(shares);

        log.info("Split expense {} added to group {} by user {}", expense.getId(), groupId, userId);
        return mapToSplitExpenseResponse(expense, shares);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SplitExpenseResponse> getGroupExpenses(Long userId, Long groupId) {
        verifyMembership(userId, groupId);
        List<SplitExpense> expenses = expenseRepository.findByGroupIdOrderByExpenseDateDesc(groupId);

        // Fetch all shares in one query to avoid N+1
        List<SplitExpenseShare> allShares = shareRepository.findAllByGroupId(groupId);
        Map<Long, List<SplitExpenseShare>> sharesByExpenseId = allShares.stream()
                .collect(Collectors.groupingBy(s -> s.getSplitExpense().getId()));

        return expenses.stream()
                .map(e -> mapToSplitExpenseResponse(e, sharesByExpenseId.getOrDefault(e.getId(), Collections.emptyList())))
                .toList();
    }

    // ==================================================================================
    // BALANCES (MATH ENGINE)
    // ==================================================================================

    @Override
    @Transactional(readOnly = true)
    public GroupBalancesResponse getGroupBalances(Long userId, Long groupId) {
        verifyMembership(userId, groupId);

        List<GroupMember> members = memberRepository.findByGroupId(groupId);
        Map<Long, BigDecimal> net = new HashMap<>();
        Map<Long, String> names = new HashMap<>();

        for (GroupMember m : members) {
            net.put(m.getId(), BigDecimal.ZERO);
            names.put(m.getId(), m.getName());
        }

        List<SplitExpense> expenses = expenseRepository.findByGroupIdOrderByExpenseDateDesc(groupId);
        List<SplitExpenseShare> allShares = shareRepository.findAllByGroupId(groupId);
        Map<Long, List<SplitExpenseShare>> sharesByExpenseId = allShares.stream()
                .collect(Collectors.groupingBy(s -> s.getSplitExpense().getId()));

        // 1. Process Expenses (Paid vs Owed)
        for (SplitExpense e : expenses) {
            Long payerId = e.getPaidByMember().getId();
            net.computeIfPresent(payerId, (k, v) -> v.add(e.getAmount()));

            List<SplitExpenseShare> shares = sharesByExpenseId.getOrDefault(e.getId(), Collections.emptyList());
            for (SplitExpenseShare s : shares) {
                Long memberId = s.getMember().getId();
                net.computeIfPresent(memberId, (k, v) -> v.subtract(s.getAmount()));
            }
        }

        // 2. Process Settlements (Cash Transfers)
        List<Settlement> settlements = settlementRepository.findByGroupId(groupId);
        for (Settlement s : settlements) {
            Long from = s.getPaidByMember().getId();
            Long to = s.getPaidToMember().getId();
            net.computeIfPresent(from, (k, v) -> v.add(s.getAmount()));
            net.computeIfPresent(to, (k, v) -> v.subtract(s.getAmount()));
        }

        List<MemberBalance> balances = net.entrySet().stream()
                .map(e -> MemberBalance.builder()
                        .memberId(e.getKey())
                        .name(names.get(e.getKey()))
                        .netBalance(e.getValue())
                        .build())
                .toList();

        List<SettlementTransaction> suggested = computeSettlements(net, names);

        return GroupBalancesResponse.builder()
                .groupId(groupId)
                .memberBalances(balances)
                .suggestedSettlements(suggested)
                .build();
    }

    // ==================================================================================
    // SETTLEMENTS
    // ==================================================================================

    @Override
    @Transactional
    public SettlementResponse addSettlement(Long userId, Long groupId, CreateSettlementRequest request) {
        verifyMembership(userId, groupId);

        UserEntity currentUser = getUserOrThrow(userId);
        ExpenseGroup group = getGroupOrThrow(groupId);
        GroupMember paidBy = getMemberOrThrow(request.getPaidByMemberId());
        GroupMember paidTo = getMemberOrThrow(request.getPaidToMemberId());

        if (paidBy.getId().equals(paidTo.getId())) {
            throw new IllegalArgumentException("Cannot settle a debt with yourself");
        }

        Settlement settlement = Settlement.builder()
                .group(group)
                .paidByMember(paidBy)
                .paidToMember(paidTo)
                .amount(request.getAmount())
                .settlementDate(request.getSettlementDate())
                .created(Instant.now())
                .createdBy(currentUser)
                .build();

        settlement = settlementRepository.save(settlement);
        log.info("Settlement recorded in group {}: {} paid {}", groupId, paidBy.getId(), paidTo.getId());

        return mapToSettlementResponse(settlement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlements(Long userId, Long groupId) {
        verifyMembership(userId, groupId);
        return settlementRepository.findByGroupId(groupId).stream()
                .map(this::mapToSettlementResponse)
                .toList();
    }

    // ==================================================================================
// GROUP MANAGEMENT (UPDATE / DELETE)
// ==================================================================================

    @Override
    @Transactional
    public void updateGroup(Long userId, Long groupId, UpdateGroupRequest request) {
        verifyMembership(userId, groupId);
        ExpenseGroup group = getGroupOrThrow(groupId);
        UserEntity currentUser = getUserOrThrow(userId);

        // Only the creator can edit group details
        if (!group.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Only the group creator can edit the group details");
        }

        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setModified(Instant.now());
        // Note: You might want to add .setModifiedBy(currentUser) if you have that field
        groupRepository.save(group);
    }

    @Override
    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        verifyMembership(userId, groupId);
        ExpenseGroup group = getGroupOrThrow(groupId);

        // Only the creator can delete the entire group
        if (!group.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Only the group creator can delete this group");
        }

        // Thanks to CascadeType.ALL in the entities, this deletes all expenses, shares, and settlements
        groupRepository.delete(group);
        log.info("Group {} deleted by user {}", groupId, userId);
    }

    @Override
    @Transactional
    public void removeMember(Long userId, Long groupId, Long memberId) {
        verifyMembership(userId, groupId);
        ExpenseGroup group = getGroupOrThrow(groupId);

        if (!group.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Only the group creator can remove members");
        }

        GroupMember member = getMemberOrThrow(memberId);
        if (!member.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Member does not belong to this group");
        }
        if (member.getUser() != null && member.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot remove yourself (the creator) from the group");
        }

        // BUSINESS RULE: Prevent deleting a member who has active expenses
        boolean hasPaidExpenses = expenseRepository.existsByPaidByMemberId(memberId);
        boolean hasShares = shareRepository.existsByMemberId(memberId);
        boolean hasSettlements = settlementRepository.existsByPaidByMemberIdOrPaidToMemberId(memberId, memberId);

        if (hasPaidExpenses || hasShares || hasSettlements) {
            throw new IllegalStateException(
                    "Cannot remove " + member.getName() + ". They have active expenses or settlements. Please delete their transactions first."
            );
        }

        memberRepository.delete(member);
    }

    @Override
    @Transactional
    public void addMembersToGroup(Long userId, Long groupId, AddGroupMembersRequest request) {
        verifyMembership(userId, groupId);

        ExpenseGroup group = getGroupOrThrow(groupId);
        UserEntity currentUser = getUserOrThrow(userId);

        // 1. Get existing registered user IDs in this group to prevent duplicates
        Set<Long> existingUserIds = memberRepository.findByGroupId(groupId).stream()
                .filter(m -> m.getUser() != null)
                .map(m -> m.getUser().getId())
                .collect(Collectors.toSet());

        // 2. Add the new members
        for (GroupMemberInput input : request.getMembers()) {
            // Skip if the registered user is already in the group
            if (input.getUserId() != null && existingUserIds.contains(input.getUserId())) {
                continue;
            }

            UserEntity linkedUser = input.getUserId() != null ? getUserOrThrow(input.getUserId()) : null;

            GroupMember member = GroupMember.builder()
                    .group(group)
                    .user(linkedUser)
                    .name(input.getName())
                    .joinedAt(Instant.now())
                    .createdBy(currentUser)
                    .build();

            memberRepository.save(member);
        }

        log.info("Added {} new members to group {}", request.getMembers().size(), groupId);
    }

// ==================================================================================
// EXPENSE MANAGEMENT (UPDATE / DELETE)
// ==================================================================================

    @Override
    @Transactional
    public SplitExpenseResponse updateExpense(Long userId, Long groupId, Long expenseId, CreateSplitExpenseRequest request) {
        verifyMembership(userId, groupId);

        SplitExpense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + expenseId));

        if (!expense.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Expense does not belong to this group");
        }

        GroupMember paidByMember = getMemberOrThrow(request.getPaidByMemberId());
        if (!paidByMember.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Payer must be a member of this group");
        }

        // 1. Update basic fields
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setPaidByMember(paidByMember);
        expense.setExpenseDate(request.getExpenseDate());
        expense.setModified(Instant.now());
        expense = expenseRepository.save(expense);

        // 2. Recalculate shares (delete old, insert new)
        shareRepository.deleteAllBySplitExpenseId(expenseId);

        UserEntity currentUser = getUserOrThrow(userId);
        List<SplitExpenseShare> newShares = calculateShares(expense, groupId, request, currentUser);
        validateSharesSum(request.getAmount(), newShares);
        newShares = shareRepository.saveAll(newShares);

        return mapToSplitExpenseResponse(expense, newShares);
    }

    @Override
    @Transactional
    public void deleteExpense(Long userId, Long groupId, Long expenseId) {
        verifyMembership(userId, groupId);

        SplitExpense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Expense not found: " + expenseId));

        if (!expense.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Expense does not belong to this group");
        }

        expenseRepository.delete(expense);
        log.info("Expense {} deleted from group {}", expenseId, groupId);
    }

// ==================================================================================
// SETTLEMENT MANAGEMENT (DELETE ONLY)
// ==================================================================================

    @Override
    @Transactional
    public void deleteSettlement(Long userId, Long groupId, Long settlementId) {
        verifyMembership(userId, groupId);

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new EntityNotFoundException("Settlement not found: " + settlementId));

        if (!settlement.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Settlement does not belong to this group");
        }

        settlementRepository.delete(settlement);
    }

    // ==================================================================================
    // PRIVATE HELPERS & CALCULATIONS
    // ==================================================================================

    private void verifyMembership(Long userId, Long groupId) {
        // Ensure the user is linked to at least one member profile in this group
        if (!memberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AccessDeniedException("User is not authorized to access this group");
        }
    }

    private UserEntity getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    private ExpenseGroup getGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupId));
    }

    private GroupMember getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
    }

    private List<SplitExpenseShare> calculateShares(SplitExpense expense, Long groupId, CreateSplitExpenseRequest request, UserEntity currentUser) {
        List<SplitExpenseShare> shares = new ArrayList<>();

        // Verify all requested member IDs actually belong to this group
        Set<Long> groupMemberIds = memberRepository.findByGroupId(groupId).stream()
                .map(GroupMember::getId)
                .collect(Collectors.toSet());

        switch (request.getSplitType()) {
            case EQUAL -> {
                List<SplitShareRequest> participants = request.getShares();
                int n = participants.size();
                if (n == 0) throw new IllegalArgumentException("Must provide participants for EQUAL split");

                BigDecimal share = request.getAmount().divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
                BigDecimal remainder = request.getAmount().subtract(share.multiply(BigDecimal.valueOf(n)));

                for (int i = 0; i < n; i++) {
                    Long memberId = participants.get(i).getMemberId();
                    validateMembership(memberId, groupMemberIds);

                    // Add the remainder (e.g., $0.01) to the first person to ensure exact sum
                    BigDecimal amt = (i == 0) ? share.add(remainder) : share;
                    shares.add(buildShare(expense, memberId, amt, currentUser));
                }
            }
            case EXACT -> {
                for (SplitShareRequest s : request.getShares()) {
                    validateMembership(s.getMemberId(), groupMemberIds);
                    shares.add(buildShare(expense, s.getMemberId(), s.getAmount(), currentUser));
                }
            }
            case PERCENTAGE -> {
                for (SplitShareRequest s : request.getShares()) {
                    validateMembership(s.getMemberId(), groupMemberIds);
                    // e.g., 25.5% of 1000 = (1000 * 25.5) / 100 = 255.00
                    BigDecimal amt = request.getAmount()
                            .multiply(s.getAmount())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    shares.add(buildShare(expense, s.getMemberId(), amt, currentUser));
                }
            }
        }
        return shares;
    }

    private void validateMembership(Long memberId, Set<Long> validMemberIds) {
        if (!validMemberIds.contains(memberId)) {
            throw new IllegalArgumentException("Member ID " + memberId + " does not belong to this group");
        }
    }

    private SplitExpenseShare buildShare(SplitExpense expense, Long memberId, BigDecimal amount, UserEntity currentUser) {
        return SplitExpenseShare.builder()
                .splitExpense(expense)
                .member(getMemberOrThrow(memberId))
                .amount(amount)
                .createdBy(currentUser)
                .build();
    }

    private void validateSharesSum(BigDecimal totalAmount, List<SplitExpenseShare> shares) {
        BigDecimal sum = shares.stream().map(SplitExpenseShare::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        // Allow a 5 paise/cent tolerance for rounding differences
        if (sum.subtract(totalAmount).abs().compareTo(new BigDecimal("0.05")) > 0) {
            throw new IllegalArgumentException("Sum of shares (" + sum + ") does not match total (" + totalAmount + ")");
        }
    }

    private BigDecimal computeUserNetBalanceOptimized(Long groupId, Long memberId, List<SplitExpense> preFetchedExpenses) {
        BigDecimal net = BigDecimal.ZERO;

        List<SplitExpenseShare> userShares = shareRepository.findAllByGroupId(groupId).stream()
                .filter(s -> s.getMember().getId().equals(memberId))
                .toList();

        for (SplitExpense e : preFetchedExpenses) {
            if (e.getPaidByMember().getId().equals(memberId)) {
                net = net.add(e.getAmount());
            }
        }
        for (SplitExpenseShare s : userShares) {
            net = net.subtract(s.getAmount());
        }

        for (Settlement s : settlementRepository.findByGroupId(groupId)) {
            if (s.getPaidByMember().getId().equals(memberId)) net = net.add(s.getAmount());
            if (s.getPaidToMember().getId().equals(memberId)) net = net.subtract(s.getAmount());
        }
        return net;
    }

    private List<SettlementTransaction> computeSettlements(Map<Long, BigDecimal> net, Map<Long, String> names) {
        List<Map.Entry<Long, BigDecimal>> creditors = new ArrayList<>();
        List<Map.Entry<Long, BigDecimal>> debtors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> e : net.entrySet()) {
            int cmp = e.getValue().compareTo(BigDecimal.ZERO);
            if (cmp > 0) creditors.add(e);
            else if (cmp < 0) debtors.add(e);
        }

        creditors.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        debtors.sort((a, b) -> a.getValue().compareTo(b.getValue()));

        List<SettlementTransaction> result = new ArrayList<>();
        int i = 0, j = 0;

        while (i < creditors.size() && j < debtors.size()) {
            Map.Entry<Long, BigDecimal> c = creditors.get(i);
            Map.Entry<Long, BigDecimal> d = debtors.get(j);
            BigDecimal amt = c.getValue().min(d.getValue().abs());

            if (amt.compareTo(BigDecimal.ZERO) > 0) {
                result.add(SettlementTransaction.builder()
                        .fromMemberId(d.getKey())
                        .fromName(names.get(d.getKey()))
                        .toMemberId(c.getKey())
                        .toName(names.get(c.getKey()))
                        .amount(amt)
                        .build());
            }

            c.setValue(c.getValue().subtract(amt));
            d.setValue(d.getValue().add(amt));

            if (c.getValue().compareTo(BigDecimal.ZERO) == 0) i++;
            if (d.getValue().compareTo(BigDecimal.ZERO) == 0) j++;
        }
        return result;
    }

    // ==================================================================================
    // DTO MAPPERS
    // ==================================================================================

    private GroupResponse mapToGroupResponse(ExpenseGroup group, List<GroupMember> members) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdByUserId(group.getUser().getId())
                .createdByUsername(group.getUser().getUsername())
                .created(group.getCreated())
                .members(members.stream()
                        .map(m -> {
                            boolean isCreator = m.getUser() != null && m.getUser().getId().equals(group.getUser().getId());
                            return MemberDto.builder()
                                    .id(m.getId())
                                    .userId(m.getUser() != null ? m.getUser().getId() : null)
                                    .name(m.getName())
                                    .isCreator(isCreator)
                                    .build();
                        })
                        .toList())
                .build();
    }

    private SplitExpenseResponse mapToSplitExpenseResponse(SplitExpense expense, List<SplitExpenseShare> shares) {
        return SplitExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .paidByMemberId(expense.getPaidByMember().getId())
                .paidByMemberName(expense.getPaidByMember().getName())
                .expenseDate(expense.getExpenseDate())
                .shares(shares.stream()
                        .map(s -> ShareDto.builder()
                                .memberId(s.getMember().getId())
                                .memberName(s.getMember().getName())
                                .amount(s.getAmount())
                                .build())
                        .toList())
                .build();
    }

    private SettlementResponse mapToSettlementResponse(Settlement settlement) {
        return SettlementResponse.builder()
                .id(settlement.getId())
                .paidByMemberId(settlement.getPaidByMember().getId())
                .paidByMemberName(settlement.getPaidByMember().getName())
                .paidToMemberId(settlement.getPaidToMember().getId())
                .paidToMemberName(settlement.getPaidToMember().getName())
                .amount(settlement.getAmount())
                .settlementDate(settlement.getSettlementDate())
                .build();
    }
}