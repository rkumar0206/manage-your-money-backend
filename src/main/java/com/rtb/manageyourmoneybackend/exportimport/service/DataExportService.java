package com.rtb.manageyourmoneybackend.exportimport.service;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import com.rtb.manageyourmoneybackend.expense.repository.ExpenseRepository;
import com.rtb.manageyourmoneybackend.expensecategory.entity.ExpenseCategory;
import com.rtb.manageyourmoneybackend.expensecategory.repository.ExpenseCategoryRepository;
import com.rtb.manageyourmoneybackend.exportimport.dto.CategoryExportDto;
import com.rtb.manageyourmoneybackend.exportimport.dto.ExpenseExportDto;
import com.rtb.manageyourmoneybackend.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {

    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public void triggerExportAsync(Long userId, String recipientEmail) {
        virtualThreadExecutor.submit(() -> executeExport(userId, recipientEmail));
    }

    private void executeExport(Long userId, String recipientEmail) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("user_export_" + userId + "_", ".json.gz");
            File finalTempFile = tempFile;

            transactionTemplate.executeWithoutResult(status -> {
                try (FileOutputStream fos = new FileOutputStream(finalTempFile);
                     GZIPOutputStream gzos = new GZIPOutputStream(fos);
                     JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(gzos, JsonEncoding.UTF8)) {

                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("version", "1.0");

                    // 1. Stream Categories
                    jsonGenerator.writeArrayFieldStart("categories");
                    try (Stream<ExpenseCategory> categoryStream = categoryRepository.streamByUserId(userId)) {
                        categoryStream.forEach(category -> writeCategory(jsonGenerator, category));
                    }
                    jsonGenerator.writeEndArray();

                    // 2. Stream Expenses
                    jsonGenerator.writeArrayFieldStart("expenses");
                    try (Stream<Expense> expenseStream = expenseRepository.streamByUserId(userId)) {
                        expenseStream.forEach(expense -> writeExpense(jsonGenerator, expense));
                    }
                    jsonGenerator.writeEndArray();

                    jsonGenerator.writeEndObject();
                } catch (IOException e) {
                    throw new RuntimeException("Failed writing export JSON stream", e);
                }
            });

            emailService.sendExportEmail(recipientEmail, tempFile);

        } catch (Exception e) {
            log.error("Export process failed for user ID {}", userId, e);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                if (tempFile.delete()) {
                    log.info("Temporary export file deleted successfully: {}", tempFile.getName());
                } else {
                    log.warn("Failed to delete temp file: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }

    private void writeCategory(JsonGenerator generator, ExpenseCategory category) {
        try {
            generator.writeObject(new CategoryExportDto(
                    category.getName(),
                    category.getDescription(),
                    category.getImageUrl(),
                    category.isSynced(),
                    category.getCreated(),
                    category.getModified()
            ));
        } catch (IOException e) {
            throw new RuntimeException("Error writing category record", e);
        }
    }

    private void writeExpense(JsonGenerator generator, Expense expense) {
        try {
            generator.writeObject(new ExpenseExportDto(
                    expense.getSpentOn(),
                    expense.getAmount(),
                    expense.getCategory().getName(), // Mapped via Category Name
                    expense.getPaymentMethods(),
                    expense.isSynced(),
                    expense.getCreated(),
                    expense.getModified()
            ));
        } catch (IOException e) {
            throw new RuntimeException("Error writing expense record", e);
        }
    }
}