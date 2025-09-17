package com.loopers.infrastructure.rank;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public void add() {

    }
}
