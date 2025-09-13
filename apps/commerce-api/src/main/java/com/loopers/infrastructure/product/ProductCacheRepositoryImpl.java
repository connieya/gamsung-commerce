package com.loopers.infrastructure.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.config.RedisCacheConfig;
import com.loopers.config.RedisKeyManager;
import com.loopers.domain.likes.LikeUpdateType;
import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductCacheRepositoryImpl implements ProductCacheRepository {

    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration TTL = Duration.ofSeconds(30);

    @Override
    public Page<ProductInfo> findProductDetails(Pageable pageable, Long brandId) {
        if (pageable.getPageNumber() > 2) {
            return Page.empty(pageable);
        }

        String key = RedisKeyManager.PRODUCT_LIST_KET_PREFIX + ":sort" + pageable.getSort() + ":page" + pageable.getPageNumber() + ":size" + pageable.getPageSize() + ":brandId" + (brandId != null ? brandId : "ALL");
        String json = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(json)) {
            return Page.empty(pageable);
        }

        try {
            return objectMapper.readValue(json, new TypeReference<PageImpl<ProductInfo>>() {});
        } catch (JsonProcessingException e) {
            return Page.empty(pageable);
        }

    }

    @Override
    public void save(Long brandId, Page<ProductInfo> page) {
        if (page.getNumber() > 2 || page.isEmpty()) return;

        String key = RedisKeyManager.PRODUCT_LIST_KET_PREFIX + ":sort" + page.getSort() + ":page" + page.getNumber() + ":size" + page.getSize() + ":brandId" + (brandId != null ? brandId : "ALL");

        String json;
        try {
            json = objectMapper.writeValueAsString(page);
            stringRedisTemplate.opsForValue().set(key, json, TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        stringRedisTemplate.opsForValue().set(key, json, TTL);
    }

    @Override
    public Optional<ProductDetailInfo> findProductDetailById(Long productId) {
        String key = RedisKeyManager.PRODUCT_DETAIL_KEY +productId;
        Map<String, Object> cache = objectRedisTemplate.<String, Object>opsForHash().entries(key);

        if (CollectionUtils.isEmpty(cache)) {
            return Optional.empty();
        }


        if (cache.containsKey("__null__")) {
            return Optional.of(ProductDetailInfo.EMPTY);
        }
        ProductDetailInfo productDetailInfo = objectMapper.convertValue(cache, ProductDetailInfo.class);
        return Optional.of(productDetailInfo);
    }

    @Override
    public void saveProductDetail(ProductDetailInfo productDetailInfo) {
        String key = RedisKeyManager.PRODUCT_DETAIL_KEY + productDetailInfo.getProductId();
        Duration ttl = RedisCacheConfig.jitter(Duration.ofHours(2));


        // 캐시 관통을 방지한다.
        if (productDetailInfo == null) {
            objectRedisTemplate.opsForHash().putAll(key, Map.of("__null__", "null"));
            objectRedisTemplate.expire(key, ttl);
            return;
        }


        Map<String, Object> cache = objectMapper.convertValue(productDetailInfo, new TypeReference<>() {
        });
        objectRedisTemplate.opsForHash().putAll(key, cache);
        objectRedisTemplate.expire(key, ttl);
    }

    @Override
    public void updateLikeCount(Long productId , LikeUpdateType updateType) {
        System.out.println("productId = " + productId);
        objectRedisTemplate.opsForHash().increment(RedisKeyManager.PRODUCT_DETAIL_KEY+productId, "likeCount" ,updateType ==  LikeUpdateType.INCREMENT ? 1 : -1);
    }


}
