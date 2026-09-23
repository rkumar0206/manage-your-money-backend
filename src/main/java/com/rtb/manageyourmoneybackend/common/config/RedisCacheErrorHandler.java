package com.rtb.manageyourmoneybackend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.SimpleCacheErrorHandler;

@Slf4j
public class RedisCacheErrorHandler extends SimpleCacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
        log.warn("Redis CACHE GET failed — falling back to source. cache={}, key={}, error={}",
                cache.getName(), key, ex.getMessage());
    }

    @Override
    public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
        log.warn("Redis CACHE PUT failed. cache={}, key={}, error={}",
                cache.getName(), key, ex.getMessage());
    }

    @Override
    public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
        log.warn("Redis CACHE EVICT failed. cache={}, key={}, error={}",
                cache.getName(), key, ex.getMessage());
    }

    @Override
    public void handleCacheClearError(RuntimeException ex, Cache cache) {
        log.warn("Redis CACHE CLEAR failed. cache={}, error={}",
                cache.getName(), ex.getMessage());
    }
}