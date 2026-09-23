package com.rtb.manageyourmoneybackend.common.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    /** Fallback TTL for any cache not listed under {@link #ttls}. */
    private Duration defaultTtl = Duration.ofMinutes(30);

    /** Whether to record hit/miss stats (exposed via Micrometer). */
    private boolean enableStatistics = true;

    /** Per-cache TTL overrides. Key = cache name (must match CacheConstants). */
    private Map<String, Duration> ttls = new HashMap<>();
}