package com.rtb.manageyourmoneybackend.expense.service.impl;

import com.rtb.manageyourmoneybackend.common.exception.ResourceNotFoundException;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryBreakdownItemDTO;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryBreakdownResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryMonthlyStatsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.CategoryStatsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.DailyAmountDTO;
import com.rtb.manageyourmoneybackend.expense.dto.DailyStatsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.DayOfWeekAmountDTO;
import com.rtb.manageyourmoneybackend.expense.dto.DayOfWeekStatsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseCreateRequestDTO;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseSearchRequestDTO;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseSummaryResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.ExpenseUpdateRequestDTO;
import com.rtb.manageyourmoneybackend.expense.dto.MonthlyAmountDTO;
import com.rtb.manageyourmoneybackend.expense.dto.PaymentMethodAmountDTO;
import com.rtb.manageyourmoneybackend.expense.dto.PaymentMethodDistributionResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.TopVendorsResponseDTO;
import com.rtb.manageyourmoneybackend.expense.dto.VendorAmountDTO;
import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import com.rtb.manageyourmoneybackend.expense.filter.DateRangePreset;
import com.rtb.manageyourmoneybackend.expense.mapper.ExpenseMapper;
import com.rtb.manageyourmoneybackend.expense.repository.ExpenseRepository;
import com.rtb.manageyourmoneybackend.expense.service.ExpenseService;
import com.rtb.manageyourmoneybackend.expense.specification.ExpenseSpecifications;
import com.rtb.manageyourmoneybackend.expensecategory.entity.ExpenseCategory;
import com.rtb.manageyourmoneybackend.expensecategory.repository.ExpenseCategoryRepository;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * NOTE: assumes {@code ExpenseCategoryRepository} (JpaRepository<ExpenseCategory, Long>)
 * and {@code UserRepository} (JpaRepository<UserEntity, Long>) already exist in the
 * respective packages. Adjust the imports if your actual package layout differs.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final UserRepository userRepository;
    private final ExpenseMapper expenseMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ExpenseResponseDTO create(Long userId, ExpenseCreateRequestDTO request) {
        ExpenseCategory category = resolveCategory(request.getCategoryId());
        UserEntity user = userRepository.getReferenceById(userId);

        Expense expense = expenseMapper.toEntity(request);
        expense.setCategory(category);
        expense.setUser(user);
        expense.setSynced(true);
        expense.setCreated(request.getCreated() != null ? request.getCreated() : Instant.now());
        expense.setModified(Instant.now());

        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toResponseDto(saved);
    }

    @Override
    public ExpenseResponseDTO getById(Long userId, Long id) {
        return expenseRepository.findResponseByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense not found with id: " + id + " for current user"));
    }

    @Override
    public Page<ExpenseResponseDTO> getAll(Long userId, Pageable pageable) {
        return expenseRepository.findAllResponsesByUserId(userId, pageable);
    }

    @Override
    public Page<ExpenseResponseDTO> getAllByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable) {
        return expenseRepository.findAllResponsesByUserIdAndCategoryId(userId, categoryId, pageable);
    }

    @Override
    public List<String> getDistinctPaymentMethods(Long userId) {
        return expenseRepository.findPaymentMethodsByUserId(userId).stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(method -> method != null && !method.isBlank())
                .collect(Collectors.toCollection(TreeSet::new))
                .stream()
                .toList();
    }

    @Override
    public BigDecimal getTotalAmountSpentByCategoryId(Long userId, Long categoryId) {
        return expenseRepository.sumAmountByUserIdAndCategoryId(userId, categoryId);
    }

    @Override
    public BigDecimal getTotalAmountSpentByUserId(Long userId) {
        return expenseRepository.sumAmountByUserId(userId);
    }

    @Override
    public Page<ExpenseResponseDTO> search(Long userId, ExpenseSearchRequestDTO criteria, Pageable pageable) {
        Specification<Expense> spec = ExpenseSpecifications.build(userId, criteria, true);
        return expenseRepository.findAll(spec, pageable)
                .map(expenseMapper::toResponseDto);
    }

    @Override
    public BigDecimal getTotalAmountSpentBySearchCriteria(Long userId, ExpenseSearchRequestDTO criteria) {
        Specification<Expense> spec = ExpenseSpecifications.build(userId, criteria, false);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BigDecimal> query = cb.createQuery(BigDecimal.class);
        Root<Expense> root = query.from(Expense.class);

        Predicate predicate = spec.toPredicate(root, query, cb);
        query.select(cb.coalesce(cb.sum(root.get("amount")), BigDecimal.ZERO));
        if (predicate != null) {
            query.where(predicate);
        }

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public CategoryStatsResponseDTO getCategoryTotalForDateRange(
            Long userId, Long categoryId, DateRangePreset dateRangePreset, List<String> paymentMethods) {
        ExpenseSearchRequestDTO criteria = ExpenseSearchRequestDTO.builder()
                .categoryId(categoryId)
                .dateRangePreset(dateRangePreset)
                .paymentMethods(paymentMethods)
                .build();

        BigDecimal total = getTotalAmountSpentBySearchCriteria(userId, criteria);

        return CategoryStatsResponseDTO.builder()
                .categoryId(categoryId)
                .dateRangePreset(dateRangePreset)
                .totalAmount(total)
                .build();
    }

    @Override
    public CategoryMonthlyStatsResponseDTO getMonthlyTotalsForYear(
            Long userId, int year, Long categoryId, List<String> paymentMethods) {
        ZoneId zone = ZoneId.systemDefault();
        Instant yearStart = LocalDate.of(year, 1, 1).atStartOfDay(zone).toInstant();
        Instant yearEnd = LocalDate.of(year + 1, 1, 1).atStartOfDay(zone).toInstant();

        String paymentMethodsCsv = (paymentMethods == null || paymentMethods.isEmpty())
                ? null
                : String.join(",", paymentMethods);

        Map<Integer, BigDecimal> totalsByMonth = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            totalsByMonth.put(month, BigDecimal.ZERO);
        }

        for (Object[] row : expenseRepository.findMonthlyTotals(userId, yearStart, yearEnd, categoryId, paymentMethodsCsv)) {
            int month = ((Number) row[0]).intValue();
            BigDecimal total = (BigDecimal) row[1];
            totalsByMonth.put(month, total);
        }

        List<MonthlyAmountDTO> months = totalsByMonth.entrySet().stream()
                .map(entry -> MonthlyAmountDTO.builder()
                        .month(entry.getKey())
                        .monthName(Month.of(entry.getKey()).getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                        .amount(entry.getValue())
                        .build())
                .toList();

        return CategoryMonthlyStatsResponseDTO.builder()
                .year(year)
                .categoryId(categoryId)
                .months(months)
                .build();
    }

    @Override
    public CategoryBreakdownResponseDTO getCategoryBreakdown(Long userId, DateRangePreset dateRangePreset, Integer topN) {
        Instant[] range = (dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME).resolve();

        List<Object[]> rows = expenseRepository.findCategoryBreakdown(userId, range[0], range[1]);

        BigDecimal grandTotal = rows.stream()
                .map(row -> (BigDecimal) row[2])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryBreakdownItemDTO> categories = rows.stream()
                .map(row -> {
                    BigDecimal amount = (BigDecimal) row[2];
                    BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : amount.multiply(BigDecimal.valueOf(100))
                            .divide(grandTotal, 2, RoundingMode.HALF_UP);
                    return CategoryBreakdownItemDTO.builder()
                            .categoryId(((Number) row[0]).longValue())
                            .categoryName((String) row[1])
                            .amount(amount)
                            .percentage(percentage)
                            .build();
                })
                .toList();

        // rows is already ORDER BY total DESC, so categories is too — top N is just a prefix slice.
        if (topN != null && topN > 0 && categories.size() > topN) {
            List<CategoryBreakdownItemDTO> top = categories.subList(0, topN);
            List<CategoryBreakdownItemDTO> rest = categories.subList(topN, categories.size());

            BigDecimal otherAmount = rest.stream()
                    .map(CategoryBreakdownItemDTO::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal otherPercentage = grandTotal.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : otherAmount.multiply(BigDecimal.valueOf(100)).divide(grandTotal, 2, RoundingMode.HALF_UP);

            CategoryBreakdownItemDTO other = CategoryBreakdownItemDTO.builder()
                    .categoryId(null)
                    .categoryName("Other")
                    .amount(otherAmount)
                    .percentage(otherPercentage)
                    .build();

            List<CategoryBreakdownItemDTO> combined = new ArrayList<>(top);
            combined.add(other);
            categories = combined;
        }

        return CategoryBreakdownResponseDTO.builder()
                .dateRangePreset(dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME)
                .totalAmount(grandTotal)
                .categories(categories)
                .build();
    }

    @Override
    public PaymentMethodDistributionResponseDTO getPaymentMethodDistribution(Long userId, DateRangePreset dateRangePreset) {
        Instant[] range = (dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME).resolve();

        List<PaymentMethodAmountDTO> paymentMethods = expenseRepository
                .findPaymentMethodTotals(userId, range[0], range[1]).stream()
                .map(row -> PaymentMethodAmountDTO.builder()
                        .paymentMethod((String) row[0])
                        .amount((BigDecimal) row[1])
                        .build())
                .toList();

        return PaymentMethodDistributionResponseDTO.builder()
                .dateRangePreset(dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME)
                .paymentMethods(paymentMethods)
                .build();
    }

    @Override
    public DayOfWeekStatsResponseDTO getDayOfWeekStats(Long userId, DateRangePreset dateRangePreset) {
        Instant[] range = (dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME).resolve();

        Map<Integer, BigDecimal> totalsByIsoDow = new LinkedHashMap<>();
        for (int isoDow = 1; isoDow <= 7; isoDow++) {
            totalsByIsoDow.put(isoDow, BigDecimal.ZERO);
        }

        for (Object[] row : expenseRepository.findDayOfWeekTotals(userId, range[0], range[1])) {
            int isoDow = ((Number) row[0]).intValue();
            BigDecimal total = (BigDecimal) row[1];
            totalsByIsoDow.put(isoDow, total);
        }

        List<DayOfWeekAmountDTO> days = totalsByIsoDow.entrySet().stream()
                .map(entry -> {
                    DayOfWeek dayOfWeek = DayOfWeek.of(entry.getKey());
                    return DayOfWeekAmountDTO.builder()
                            .dayOfWeek(dayOfWeek)
                            .dayLabel(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                            .amount(entry.getValue())
                            .build();
                })
                .toList();

        return DayOfWeekStatsResponseDTO.builder()
                .dateRangePreset(dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME)
                .days(days)
                .build();
    }

    @Override
    public TopVendorsResponseDTO getTopVendors(Long userId, DateRangePreset dateRangePreset, int limit) {
        Instant[] range = (dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME).resolve();

        List<VendorAmountDTO> vendors = expenseRepository
                .findTopVendors(userId, range[0], range[1], limit).stream()
                .map(row -> VendorAmountDTO.builder()
                        .spentOn((String) row[0])
                        .amount((BigDecimal) row[1])
                        .build())
                .toList();

        return TopVendorsResponseDTO.builder()
                .dateRangePreset(dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME)
                .limit(limit)
                .vendors(vendors)
                .build();
    }

    @Override
    public ExpenseSummaryResponseDTO getSummary(Long userId, DateRangePreset dateRangePreset) {
        Instant[] range = (dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME).resolve();
        Object[] result = expenseRepository.findSummary(userId, range[0], range[1]);

        // Unwrap row[0] if Spring Data returned a nested array
        Object[] row = (result.length > 0 && result[0] instanceof Object[] inner) ? inner : result;

        long totalCount = row[0] != null ? ((Number) row[0]).longValue() : 0L;
        BigDecimal totalAmount = toBigDecimal(row[1]);
        BigDecimal avgAmount = toBigDecimal(row[2]).setScale(2, RoundingMode.HALF_UP);
        BigDecimal largestExpense = toBigDecimal(row[3]);
        BigDecimal minAmount = toBigDecimal(row[4]);

        return ExpenseSummaryResponseDTO.builder()
                .dateRangePreset(dateRangePreset != null ? dateRangePreset : DateRangePreset.ALL_TIME)
                .totalAmount(totalAmount)
                .totalCount(totalCount)
                .avgAmount(avgAmount)
                .largestExpense(largestExpense)
                .minAmount(minAmount)
                .build();
    }

    /**
     * JPQL's avg(...) return type isn't guaranteed to be BigDecimal across every
     * Hibernate version (it can come back as Double), so this normalizes whatever
     * Number comes back into a BigDecimal via its String form, rather than risking
     * precision loss from a Double->BigDecimal cast.
     */
    private BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> BigDecimal.ZERO;
            case BigDecimal bd -> bd;
            case Number num -> BigDecimal.valueOf(num.doubleValue());
            default -> new BigDecimal(value.toString());
        };
    }

    @Override
    public DailyStatsResponseDTO getDailyStats(Long userId, int year) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDateExclusive = LocalDate.of(year + 1, 1, 1);
        Instant yearStart = startDate.atStartOfDay(zone).toInstant();
        Instant yearEnd = endDateExclusive.atStartOfDay(zone).toInstant();

        Map<LocalDate, BigDecimal> totalsByDay = new LinkedHashMap<>();
        for (LocalDate date = startDate; date.isBefore(endDateExclusive); date = date.plusDays(1)) {
            totalsByDay.put(date, BigDecimal.ZERO);
        }

        for (Object[] row : expenseRepository.findDailyTotals(userId, yearStart, yearEnd)) {
            LocalDate day = row[0] instanceof java.sql.Date sqlDate
                    ? sqlDate.toLocalDate()
                    : (LocalDate) row[0];

            BigDecimal total = toBigDecimal(row[1]);
            totalsByDay.put(day, total);
        }

        List<DailyAmountDTO> days = totalsByDay.entrySet().stream()
                .map(entry -> DailyAmountDTO.builder()
                        .date(entry.getKey())
                        .amount(entry.getValue())
                        .build())
                .toList();

        return DailyStatsResponseDTO.builder()
                .year(year)
                .days(days)
                .build();
    }

    @Override
    @Transactional
    public ExpenseResponseDTO update(Long userId, Long id, ExpenseUpdateRequestDTO request) {
        Expense expense = findOwnedOrThrow(userId, id);

        expenseMapper.updateEntityFromDto(request, expense);

        if (request.getCategoryId() != null
                && !Objects.equals(request.getCategoryId(), expense.getCategory().getId())) {
            expense.setCategory(resolveCategory(request.getCategoryId()));
        }

        expense.setModified(Instant.now());

        Expense updated = expenseRepository.save(expense);
        return expenseMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        if (!expenseRepository.existsByIdAndUserId(id, userId)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id + " for current user");
        }
        expenseRepository.deleteById(id);
    }

    private Expense findOwnedOrThrow(Long userId, Long id) {
        return expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense not found with id: " + id + " for current user"));
    }

    private ExpenseCategory resolveCategory(Long categoryId) {
        return expenseCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "ExpenseCategory not found with id: " + categoryId));
    }
}