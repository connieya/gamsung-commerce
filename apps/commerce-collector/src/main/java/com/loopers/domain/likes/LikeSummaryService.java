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

        record TargetKey(Long targetId, LikeTargetType targetType) {}

        Map<TargetKey, Long> countChanges = items.stream()
                .collect(Collectors.groupingBy(
                        item -> new TargetKey(item.targetId(), item.targetType()),
                        Collectors.summingLong(item -> item.updateType() == LikeUpdateType.INCREMENT ? 1L : -1L)
                ));

        log.info("countChanges = {}", countChanges);

        for (Map.Entry<TargetKey, Long> entry : countChanges.entrySet()) {
            TargetKey key = entry.getKey();
            Long likeChanged = entry.getValue();
            String redisKey = "product-like:" + key.targetId();
            log.info("targetId = {} , targetType = {} , likeChanged = {} ", key.targetId(), key.targetType(), likeChanged);

            LikeTarget target = LikeTarget.create(key.targetId(), key.targetType());
            if (likeSummaryRepository.findByTarget(target).isEmpty()) {
                likeSummaryRepository.save(LikeSummary.create(key.targetId(), key.targetType()));
            }
            likeSummaryRepository.updateLikeCountBy(key.targetId(), key.targetType(), likeChanged);
            objectRedisTemplate.opsForHash().increment(redisKey, "likeCount", likeChanged);
        }
    }
}
