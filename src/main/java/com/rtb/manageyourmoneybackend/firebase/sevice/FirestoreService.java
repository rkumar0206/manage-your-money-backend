package com.rtb.manageyourmoneybackend.firebase.sevice;

import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.rtb.manageyourmoneybackend.common.cache.CacheNameConstants;
import com.rtb.manageyourmoneybackend.firebase.models.PaymentMethodItem;
import com.rtb.manageyourmoneybackend.firebase.util.FirestoreMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class FirestoreService {

    private final Firestore firestore;

    public FirestoreService(Firestore firestore) {
        this.firestore = firestore;
    }

    @Cacheable(
            cacheNames = CacheNameConstants.UID,
            key = "'uid: ' + #email"
    )
    public String getUid(String email) {
        try {
            UserRecord user = FirebaseAuth.getInstance()
                    .getUserByEmail(email.trim().toLowerCase());

            return user.getUid();

        } catch (FirebaseAuthException e) {
            log.error("Lookup failed: {} - {}", e.getAuthErrorCode(), e.getMessage());
        }
        return null;
    }

    /** Save any annotated model (full overwrite). */
    @Async
    public void save(Object model) throws ExecutionException, InterruptedException {
        save(model, false);
    }

    /** merge = true updates only the model's fields and keeps others untouched. */
    @Async
    public void save(Object model, boolean merge) throws ExecutionException, InterruptedException {
        String collection = FirestoreMapper.getCollection(model.getClass());
        String id = FirestoreMapper.getId(model);
        save(collection, id, FirestoreMapper.toMap(model), merge);
    }

    /** Lower-level: when you don't want annotations. */
    @Async
    public void save(String collection, String id, Map<String, Object> data, boolean merge)
            throws ExecutionException, InterruptedException {

        log.info("Saving data for collection: {} with data: {}", collection, data);

        DocumentReference ref = firestore.collection(collection).document(id);
        WriteResult result = merge ? ref.set(data, SetOptions.merge()).get()
                : ref.set(data).get();
        log.info("Saved {}/{} at {}", collection, id, result.getUpdateTime());
    }

    /** Batch save, chunked under Firestore's 500-write limit. */
    @Async
    public <T> void saveAll(List<T> models) throws ExecutionException, InterruptedException {
        for (int i = 0; i < models.size(); i += 400) {
            WriteBatch batch = firestore.batch();
            for (T m : models.subList(i, Math.min(i + 400, models.size()))) {
                String collection = FirestoreMapper.getCollection(m.getClass());
                String id = FirestoreMapper.getId(m);
                batch.set(firestore.collection(collection).document(id), FirestoreMapper.toMap(m));
            }
            batch.commit().get();
        }
    }

    @Async
    public void delete(Class<?> type, String id) throws ExecutionException, InterruptedException {
        firestore.collection(FirestoreMapper.getCollection(type)).document(id).delete().get();
    }

    public <T> List<T> findAllByUid(Class<T> type, String uid) throws ExecutionException, InterruptedException {

        String collection = FirestoreMapper.getCollection(type);

        QuerySnapshot snapshot = firestore.collection(collection)
                .whereEqualTo("uid", uid)
                .get()
                .get();

        List<T> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            T item = doc.toObject(type);              // needs a no-arg constructor + setters
            FirestoreMapper.setId(item, doc.getId()); // fills @FirestoreId (key)
            result.add(item);
        }
        return result;
    }

    @Cacheable(
            cacheNames = CacheNameConstants.PAYMENT_METHODS_FIRESTORE,
            key = "'payment-methods-uid-' + #uid"
    )
    public List<PaymentMethodItem> getPaymentMethods(String uid)
            throws ExecutionException, InterruptedException {
        return findAllByUid(PaymentMethodItem.class, uid);
    }
}