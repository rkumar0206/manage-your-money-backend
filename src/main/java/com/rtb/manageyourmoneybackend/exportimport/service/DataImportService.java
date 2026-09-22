package com.rtb.manageyourmoneybackend.exportimport.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import com.rtb.manageyourmoneybackend.expense.repository.ExpenseRepository;
import com.rtb.manageyourmoneybackend.expensecategory.entity.ExpenseCategory;
import com.rtb.manageyourmoneybackend.expensecategory.repository.ExpenseCategoryRepository;
import com.rtb.manageyourmoneybackend.exportimport.dto.CategoryExportDto;
import com.rtb.manageyourmoneybackend.exportimport.dto.ExpenseExportDto;
import com.rtb.manageyourmoneybackend.user.model.UserEntity;
import com.rtb.manageyourmoneybackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataImportService {

    private static final int BATCH_SIZE = 500;

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    public void importUserData(Long userId, MultipartFile file) throws IOException {
        UserEntity userRef = userRepository.getReferenceById(userId);

        try (InputStream is = getDecompressedInputStream(file.getInputStream());
             JsonParser parser = objectMapper.getFactory().createParser(is)) {

            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IllegalArgumentException("Invalid JSON format: Expected root object");
            }

            Map<String, ExpenseCategory> categoryCache = new HashMap<>();

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();
                parser.nextToken(); // Move to field value

                if ("categories".equals(fieldName)) {
                    processCategories(parser, userId, userRef, categoryCache);
                } else if ("expenses".equals(fieldName)) {
                    processExpenses(parser, userId, userRef, categoryCache);
                } else {
                    parser.skipChildren();
                }
            }
        }
    }

    /* ==========================================
     * CATEGORY PROCESSING (UPSERT IDEMPOTENT)
     * ========================================== */
    private void processCategories(JsonParser parser, Long userId, UserEntity userRef, Map<String, ExpenseCategory> categoryCache) throws IOException {
        if (parser.currentToken() != JsonToken.START_ARRAY) return;

        List<CategoryExportDto> categoryBatch = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            CategoryExportDto dto = parser.readValueAs(CategoryExportDto.class);
            categoryBatch.add(dto);

            if (categoryBatch.size() >= BATCH_SIZE) {
                persistCategoryBatch(categoryBatch, userId, userRef, categoryCache);
                categoryBatch.clear();
            }
        }

        if (!categoryBatch.isEmpty()) {
            persistCategoryBatch(categoryBatch, userId, userRef, categoryCache);
        }
    }

    private void persistCategoryBatch(List<CategoryExportDto> dtos, Long userId, UserEntity userRef, Map<String, ExpenseCategory> categoryCache) {
        transactionTemplate.executeWithoutResult(status -> {
            for (CategoryExportDto dto : dtos) {
                ExpenseCategory category = categoryRepository.findByUserIdAndName(userId, dto.name())
                        .map(existing -> {
                            existing.setDescription(dto.description());
                            existing.setImageUrl(dto.imageUrl());
                            existing.setSynced(dto.isSynced());
                            existing.setCreated(dto.created());
                            existing.setModified(dto.modified());
                            return existing;
                        })
                        .orElseGet(() -> ExpenseCategory.builder()
                                .name(dto.name())
                                .description(dto.description())
                                .imageUrl(dto.imageUrl())
                                .isSynced(dto.isSynced())
                                .user(userRef)
                                .created(dto.created())
                                .modified(dto.modified())
                                .build());

                ExpenseCategory saved = categoryRepository.save(category);
                categoryCache.put(saved.getName(), saved);
            }
        });
    }

    /* ==========================================
     * EXPENSE PROCESSING (BATCHED IDEMPOTENT)
     * ========================================== */
    private void processExpenses(JsonParser parser, Long userId, UserEntity userRef, Map<String, ExpenseCategory> categoryCache) throws IOException {
        if (parser.currentToken() != JsonToken.START_ARRAY) return;

        List<ExpenseExportDto> expenseBatch = new ArrayList<>();

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            ExpenseExportDto dto = parser.readValueAs(ExpenseExportDto.class);
            expenseBatch.add(dto);

            if (expenseBatch.size() >= BATCH_SIZE) {
                persistExpenseBatch(expenseBatch, userId, userRef, categoryCache);
                expenseBatch.clear();
            }
        }

        if (!expenseBatch.isEmpty()) {
            persistExpenseBatch(expenseBatch, userId, userRef, categoryCache);
        }
    }

    private void persistExpenseBatch(List<ExpenseExportDto> dtos, Long userId, UserEntity userRef, Map<String, ExpenseCategory> categoryCache) {
        transactionTemplate.executeWithoutResult(status -> {
            Set<Long> categoryIds = new HashSet<>();
            Map<ExpenseExportDto, ExpenseCategory> dtoToCategoryMap = new HashMap<>();

            for (ExpenseExportDto dto : dtos) {
                ExpenseCategory category = categoryCache.computeIfAbsent(dto.categoryName(),
                        name -> categoryRepository.findByUserIdAndName(userId, name)
                                .orElseThrow(() -> new IllegalStateException("Category not found: " + name)));

                categoryIds.add(category.getId());
                dtoToCategoryMap.put(dto, category);
            }

            // Fetch existing fingerprints in bulk to prevent N+1 queries
            Set<String> existingFingerprints = expenseRepository.findExistingFingerprints(userId, categoryIds);

            List<Expense> newExpenses = new ArrayList<>();

            for (ExpenseExportDto dto : dtos) {
                ExpenseCategory category = dtoToCategoryMap.get(dto);
                String fingerprint = generateFingerprint(category.getId(), dto);

                if (!existingFingerprints.contains(fingerprint)) {
                    Expense expense = Expense.builder()
                            .spentOn(dto.spentOn())
                            .amount(dto.amount())
                            .category(category)
                            .paymentMethods(dto.paymentMethods())
                            .isSynced(dto.isSynced())
                            .user(userRef)
                            .created(dto.created())
                            .modified(dto.modified())
                            .build();

                    newExpenses.add(expense);
                    existingFingerprints.add(fingerprint); // Deduplicate within the same import file
                }
            }

            if (!newExpenses.isEmpty()) {
                expenseRepository.saveAll(newExpenses);
            }
        });
    }

    private String generateFingerprint(Long categoryId, ExpenseExportDto dto) {
        return categoryId + ":" + dto.amount() + ":" +
                (dto.spentOn() != null ? dto.spentOn() : "");
    }

    /* ==========================================
     * AUTOMATIC GZIP / RAW STREAM DETECTOR
     * ========================================== */
    private InputStream getDecompressedInputStream(InputStream originalStream) throws IOException {
        PushbackInputStream pushbackStream = new PushbackInputStream(new BufferedInputStream(originalStream), 2);
        byte[] signature = new byte[2];
        int bytesRead = pushbackStream.read(signature, 0, 2);

        if (bytesRead == 2) {
            pushbackStream.unread(signature, 0, bytesRead);
            int head = ((int) signature[0] & 0xff) | ((signature[1] << 8) & 0xff00);
            if (GZIPInputStream.GZIP_MAGIC == head) {
                return new GZIPInputStream(pushbackStream);
            }
        }
        return pushbackStream;
    }
}