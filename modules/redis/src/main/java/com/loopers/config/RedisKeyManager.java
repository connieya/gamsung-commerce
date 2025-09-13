package com.loopers.config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class RedisKeyManager {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");
    public static final String RANK_PRODUCT_KEY = "rank:product:all:";
    public static final String PRODUCT_DETAIL_KEY = "product:detail:";
    public static final String PRODUCT_LIST_KET_PREFIX = "product:list:";

    public static String RankingKeyFor(LocalDate date) {
        return RANK_PRODUCT_KEY + date.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd }
    }
}
