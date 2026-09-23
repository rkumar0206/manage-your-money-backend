package com.rtb.manageyourmoneybackend.common.config;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import static com.rtb.manageyourmoneybackend.common.config.CacheConstants.*;


@Configuration(proxyBeanMethods = false)
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    /**
     * Cache key prefix used by Spring's RedisCacheManager.
     */
    public static final String CACHE_KEY_PREFIX = "manageyourmoneybackend::";

    @Bean
    public RedisCacheConfiguration defaultCacheConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                //.disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)))
                .computePrefixWith(cacheName -> CACHE_KEY_PREFIX + cacheName + "::");
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf,
                                          RedisCacheConfiguration defaultCacheConfiguration) {
        return RedisCacheManager.builder(cf)
                .cacheDefaults(defaultCacheConfiguration)
                .withCacheConfiguration(EXPENSE_CATEGORY_BY_ID, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(60)))
                .withCacheConfiguration(EXPENSE_CATEGORY_LIST, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration(EXPENSE_BY_ID, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration(EXPENSE_LIST, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(EXPENSE_AGGREGATES, defaultCacheConfiguration.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration(EXPENSE_PAYMENT_METHODS, defaultCacheConfiguration.entryTtl(Duration.ofHours(1)))
                .enableStatistics()
                .build();
    }

    /**
     * Redis-down resilience: register our custom error handler.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new RedisCacheErrorHandler();
    }
}