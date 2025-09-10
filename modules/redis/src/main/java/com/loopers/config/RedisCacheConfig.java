package com.loopers.config;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class RedisCacheConfig {

    public static Duration jitter(Duration ttl) {
        long ms = ttl.toMillis();
        long delta = ThreadLocalRandom.current().nextLong(-ms / 10, ms / 10 + 1); // ±10%
        return Duration.ofMillis(ms + delta);
    }
}
