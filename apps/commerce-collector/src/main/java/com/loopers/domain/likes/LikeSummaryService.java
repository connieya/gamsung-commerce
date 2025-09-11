package com.loopers.domain.likes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeSummaryService {

    private final LikeSummaryRepository likeSummaryRepository;
    private final RedisTemplate<String, Object> objectRedisTemplate;

    @Transactional
    public void update(LikeCommand.Update command) {
        List<LikeCommand.Update.Item> items = command.items();
        Map<Long, Long> countChanges = items.stream()
                .collect(Collectors.groupingBy(
                        LikeCommand.Update.Item::productId,
                        Collectors.summingLong(item -> item.updateType() == LikeUpdateType.INCREMENT ? 1L : -1L)
                ));
        log.info("countChanges = {}", countChanges);
        for (Long productId : countChanges.keySet()) {
            Long likeChanged = countChanges.get(productId);
            String key = "product-like:" + productId;
            log.info("productId = {} , likeChanged = {} ", productId, likeChanged);
            likeSummaryRepository.updateLikeCountBy(productId, likeChanged);
            objectRedisTemplate.opsForHash().increment(key, "likeCount", likeChanged);

        }
    }
}
