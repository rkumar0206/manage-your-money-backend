package com.rtb.manageyourmoneybackend.common.cache;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;


@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@RequiredArgsConstructor
public class RedisCacheConfig implements CachingConfigurer {

    /**
     * Cache key prefix used by Spring's RedisCacheManager.
     */
    public static final String CACHE_KEY_PREFIX = "manageyourmoneybackend::";
    private final CacheProperties cacheProperties;

    /** Plain, deterministic cache ObjectMapper with JSR-310 + default typing. */
    private ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheProperties.getDefaultTtl())
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(cacheObjectMapper())))
                .computePrefixWith(cacheName -> CACHE_KEY_PREFIX + cacheName + "::");
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf, RedisCacheConfiguration base) {

        var builder = RedisCacheManager.builder(cf).cacheDefaults(base);

        // Apply per-cache TTLs from application.yml
        for (Map.Entry<String, Duration> entry : cacheProperties.getTtls().entrySet()) {
            String name = entry.getKey();
            Duration ttl = entry.getValue();

            if (!CacheNameConstants.ALL.contains(name)) {
                throw new IllegalStateException(
                        "Unknown cache name in app.cache.ttls: '" + name + "'. "
                                + "Known caches: " + CacheNameConstants.ALL);
            }
            log.info("Cache TTL override: {} -> {}", name, ttl);
            builder.withCacheConfiguration(name, base.entryTtl(ttl));
        }

        if (cacheProperties.isEnableStatistics()) {
            builder.enableStatistics();
        }

        return builder.build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }
}