package com.loopers.application.like;

import com.loopers.domain.like.*;
import com.loopers.domain.like.port.ProductPort;
import com.loopers.domain.like.port.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LikeFacade {

    private final UserPort userPort;
    private final ProductPort productPort;
    private final LikeService likeService;
    private final LikeSummaryRepository likeSummaryRepository;

    public LikeInfo add(String userId, Long targetId, LikeTargetType targetType) {
        UserPort.UserInfo user = userPort.getUser(userId);
        return likeService.add(user.id(), targetId, targetType);
    }

    public LikeInfo remove(String userId, Long targetId, LikeTargetType targetType) {
        UserPort.UserInfo user = userPort.getUser(userId);
        return likeService.remove(user.id(), targetId, targetType);
    }

    @Transactional(readOnly = true)
    public LikeResult.LikedProducts getLikedProducts(String userId, LikeTargetType targetType) {
        UserPort.UserInfo user = userPort.getUser(userId);
        List<Like> likes = likeService.findByUserIdAndTargetType(user.id(), targetType);
        List<Long> targetIds = likes.stream()
                .map(l -> l.getTarget().getId())
                .toList();

        if (targetIds.isEmpty()) {
            return new LikeResult.LikedProducts(List.of());
        }

        List<ProductPort.ProductInfo> products = productPort.getProducts(targetIds);

        List<LikeTarget> targets = targetIds.stream()
                .map(id -> LikeTarget.create(id, targetType))
                .toList();
        Map<Long, Long> countMap = likeSummaryRepository.findByTargets(targets).stream()
                .collect(Collectors.toMap(
                        s -> s.getTarget().getId(),
                        LikeSummary::getLikeCount
                ));

        List<LikeResult.LikedProducts.LikedProductItem> items = products.stream()
                .map(p -> new LikeResult.LikedProducts.LikedProductItem(
                        p.id(),
                        p.name(),
                        p.price(),
                        p.brandName(),
                        p.imageUrl(),
                        countMap.getOrDefault(p.id(), 0L)
                ))
                .toList();

        return new LikeResult.LikedProducts(items);
    }
}
