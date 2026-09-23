package com.rtb.manageyourmoneybackend.common.cache;

import com.rtb.manageyourmoneybackend.common.config.RedisCacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEvictionService {

    private final StringRedisTemplate redisTemplate;

    /**
     * Evicts every key for the given cache whose user segment equals {@code userId}.
     * Assumes key layout:  myapp::<cacheName>::<userId>:<rest...>
     */
    public void evictByUser(String cacheName, Long userId) {
        String pattern = RedisCacheConfig.CACHE_KEY_PREFIX + cacheName + "::" + userId + ":*";
        Set<String> keys = scan(pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Evicted {} keys from [{}] for userId={}", keys.size(), cacheName, userId);
        }
    }

    /** Evicts multiple caches for a single user. */
    public void evictByUser(Long userId, String... cacheNames) {
        for (String cache : cacheNames) {
            evictByUser(cache, userId);
        }
    }

    private Set<String> scan(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
        try (Cursor<byte[]> cursor = redisTemplate.executeWithStickyConnection(
                connection -> connection.keyCommands().scan(options))) {
            if (cursor == null) return keys;
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            // Redis down → swallow, log. Nothing to evict anyway.
            log.warn("SCAN failed for pattern [{}]: {}", pattern, ex.getMessage());
        }
        return keys;
    }
}