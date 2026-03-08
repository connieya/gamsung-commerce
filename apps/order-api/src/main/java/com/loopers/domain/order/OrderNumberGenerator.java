package com.loopers.domain.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNumberGenerator {
    private OrderNumberGenerator() {}

    /**
     * Musinsa-like numeric order number: yyyyMMddHHmmss + 4-digit suffix (총 18자리)
     * 예) 202602162217020004
     */
    public static String generate() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int suffix = ThreadLocalRandom.current().nextInt(0, 10000);
        return timestamp + String.format("%04d", suffix);
    }
}

