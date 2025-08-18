package com.loopers.infrastructure.brand;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    @Override
    public Optional<Brand> findById(Long brandId) {
        String key = CACHE_KEY_PREFIX + brandId;
        String cachedData = redisTemplate.opsForValue().get(key);
        if (cachedData != null) {
            try {
                Brand brand = objectMapper.readValue(cachedData, Brand.class);
                return Optional.of(brand);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(Brand brand) {
        String key = CACHE_KEY_PREFIX + brand.getId();
        String brandJson = null;
        try {
            brandJson = objectMapper.writeValueAsString(brand);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        redisTemplate.opsForValue().set(key, brandJson);
    }
}
