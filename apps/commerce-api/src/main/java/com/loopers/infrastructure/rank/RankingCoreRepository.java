package com.loopers.infrastructure.rank;

import com.loopers.config.RedisKeyManager;
import com.loopers.domain.rank.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RankingCoreRepository implements RankingRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Set<String> findProductRanking(LocalDate date, int page, int size) {
        long startIndex = (long) (page - 1) * size;
        return redisTemplate.opsForZSet().reverseRange(RedisKeyManager.RankingKeyFor(date), startIndex, startIndex + size - 1);
    }

    @Override
    public Long findProductRank(LocalDate date, Long productId) {
        return redisTemplate.opsForZSet().reverseRank(RedisKeyManager.RankingKeyFor(date), productId.toString());
    }
}
