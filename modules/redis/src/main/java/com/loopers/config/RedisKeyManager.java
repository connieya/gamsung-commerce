package com.loopers.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RedisKeyManager {

    public static final String RANK_PRODUCT_KEY = "rank:product:all:";

    public static String keyFor(LocalDate date) {
        return RANK_PRODUCT_KEY + date.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd }
    }
}
