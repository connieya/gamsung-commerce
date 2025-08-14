package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BrandCacheRepositoryImpl implements BrandCacheRepository {

    private static final String CACHE_KEY_PREFIX = "brand:detail:";
    private final RedisTemplate<String , Brand> redisTemplate;


    @Override
    public Optional<Brand> findById(Long brandId) {
        String key = CACHE_KEY_PREFIX+brandId;
        Brand cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        return Optional.empty();
    }

    @Override
    public void save(Brand brand) {
        String key = CACHE_KEY_PREFIX + brand.getId();
        redisTemplate.opsForValue().set(key, brand);
    }
}
