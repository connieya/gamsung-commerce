package com.loopers.domain.metrics;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_PREFIX = "rank:product:all:";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int RETENTION_DAYS = 3;

    @Transactional
    public void aggregate(MetricCommand.Aggregate command) {
        List<MetricCommand.Aggregate.Item> items = command.items();

        List<ProductMetrics> productMetrics = items.stream()
                .map(item ->
                        ProductMetrics
                                .builder()
                                .date(item.date())
                                .productId(item.productId())
                                .viewCount(item.viewCount())
                                .likeCount(item.likeCount())
                                .saleQuantity(item.saleQuantity())
                                .build()
                ).toList();


        // 날짜별로 묶어서 처리 (키/만료 1회)
        Map<LocalDate, List<ProductMetrics>> byDate = productMetrics.stream()
                .collect(Collectors.groupingBy(ProductMetrics::getDate));

        byDate.forEach((date, metricsOfDate) -> {
            String key = keyFor(date);
            BoundZSetOperations<String, String> z = redisTemplate.boundZSetOps(key);

            for (ProductMetrics target : metricsOfDate) {
                double score = target.calculateRankingScore();
                z.incrementScore(String.valueOf(target.getProductId()), score);
                metricRepository.upsert(target);
            }

            // 날짜별로 한 번만 만료 설정 (3일 뒤 KST 자정)
            expireAtMidnightAfterDays(key, date);
        });
    }

    private String keyFor(LocalDate date) {
        return KEY_PREFIX + date.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd }
    }

    private void expireAtMidnightAfterDays(String key, LocalDate date) {
        var when = date.plusDays(RETENTION_DAYS).atStartOfDay(KST).toInstant(); // 3일 뒤 KST 자정
        redisTemplate.expireAt(key, Date.from(when));
    }
}
