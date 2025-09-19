package com.loopers.infrastructure.rank;

import com.loopers.config.RedisKeyManager;
import com.loopers.domain.metrics.ProductMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class RankingRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void add(LocalDate date , ProductMetrics productMetrics) {
        double score = productMetrics.calculateRankingScore();
        redisTemplate.opsForZSet().incrementScore(RedisKeyManager.RankingKeyFor(date), String.valueOf(productMetrics.getProductId()), score);
    }
}
