package com.rtb.manageyourmoneybackend.firebase.firestore.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.common.constant.Constants;
import com.rtb.manageyourmoneybackend.common.enums.FirestoreCollection;
import com.rtb.manageyourmoneybackend.common.sync.entity.SyncMetadata;
import com.rtb.manageyourmoneybackend.common.sync.repository.SyncMetadataRepository;
import com.rtb.manageyourmoneybackend.expense.entity.Expense;
import com.rtb.manageyourmoneybackend.expense.repository.ExpenseRepository;
import com.rtb.manageyourmoneybackend.expense_category.entity.ExpenseCategory;
import com.rtb.manageyourmoneybackend.expense_category.repository.ExpenseCategoryRepository;
import com.rtb.manageyourmoneybackend.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirestoreServiceImpl implements FirestoreService {

    private final UserService userService;
    private final Firestore firestore;
    private final SyncMetadataRepository syncMetadataRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final ExpenseRepository expenseRepository;

    private static final int BATCH_SIZE = 500;

    @Override
    @Async
    public void syncCollectionData(FirestoreCollection firestoreCollection) throws FirebaseAuthException {

        String uidFromToken = userService.getUidSecurityContext();

        if (StringUtils.hasText(uidFromToken)) {

            log.info("Starting to sync data for firestoreCollection {}", firestoreCollection.getCollectionName());

            String syncId = String.format("%s_%s", firestoreCollection.getCollectionName(), uidFromToken);
            SyncMetadata metadata = syncMetadataRepository.findById(syncId)
                    .orElse(new SyncMetadata(syncId, null));

            Long lastSync = metadata.getLastSync();

            CollectionReference collection = firestore.collection(firestoreCollection.getCollectionName());
            Query query = collection.limit(BATCH_SIZE).whereEqualTo(Constants.UID, uidFromToken);

            if (lastSync != null && lastSync > 0) {
                query = query
                        .whereGreaterThan("modified", lastSync);
            }

            DocumentSnapshot lastVisible = null;
            boolean isFinished = false;
            int totalMigrated = 0;
            boolean errorOccurred = false;

            while (!isFinished) {
                try {
                    // 1. Build Query (add cursor if not first page)
                    Query currentQuery = query;
                    if (lastVisible != null) {
                        currentQuery = query.startAfter(lastVisible);
                    }

                    // 2. Fetch from Firestore
                    ApiFuture<QuerySnapshot> future = currentQuery
                            .get();

                    List<QueryDocumentSnapshot> documents = future.get().getDocuments();

                    if (documents.isEmpty()) {
                        log.info("No more data found for firestoreCollection {}", firestoreCollection.getCollectionName());
                        break;
                    }

                    switch (firestoreCollection) {
                        case EXPENSE_CATEGORY -> saveToExpenseCategoryTable(documents);
                        case EXPENSE -> saveToExpenseTable(documents);

                        case PAYMENT_METHOD -> {
                        }
                    }

                    // 5. Update Cursor and Counters
                    totalMigrated += documents.size();
                    lastVisible = documents.getLast();

                    System.out.println("Migrated batch. Total so far: " + totalMigrated);

                } catch (Exception e) {
                    log.error("Error while syncing data for firestoreCollection {}", firestoreCollection.getCollectionName());
                    // Logic to retry or log specific failed batches
                    isFinished = true; // Stop to prevent infinite error loops
                    errorOccurred = true;
                }
            }

            if (!errorOccurred) {
                metadata.setLastSync(System.currentTimeMillis());
                syncMetadataRepository.save(metadata);
                log.info("Finished to sync data for firestoreCollection: {}", firestoreCollection.getCollectionName());
            }
        }
    }

    private void saveToExpenseTable(List<QueryDocumentSnapshot> documents) {

        List<Expense> entities = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            Expense entity = doc.toObject(Expense.class);
            entities.add(entity);
        }

        expenseRepository.saveAll(entities);
    }

    private void saveToExpenseCategoryTable(List<QueryDocumentSnapshot> documents) {

        // 3. Transform (Firestore -> Postgres Entity)
        List<ExpenseCategory> entities = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            ExpenseCategory entity = doc.toObject(ExpenseCategory.class);
            entities.add(entity);
        }

        // 4. Load (Save to Postgres)
        expenseCategoryRepository.saveAll(entities);
    }
}
