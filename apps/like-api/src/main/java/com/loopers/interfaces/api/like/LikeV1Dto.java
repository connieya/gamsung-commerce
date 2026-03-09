package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeResult;
import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikeTargetType;

import java.util.List;

public class LikeV1Dto {

    public static class Response {
        public record LikeAction(
                LikeTargetType likeTargetType,
                Long targetId,
                Long count
        ) {
            public static LikeAction from(LikeInfo likeInfo) {
                return new LikeAction(likeInfo.targetType(), likeInfo.targetId(), likeInfo.count());
            }
        }

        public record LikedProducts(
                List<LikedProduct> items
        ) {
            public static LikedProducts from(LikeResult.LikedProducts result) {
                List<LikedProduct> items = result.items().stream()
                        .map(item -> new LikedProduct(
                                item.productId(),
                                item.productName(),
                                item.productPrice(),
                                item.brandName(),
                                item.imageUrl(),
                                item.likeCount()
                        ))
                        .toList();
                return new LikedProducts(items);
            }

            public record LikedProduct(
                    Long productId,
                    String productName,
                    Long productPrice,
                    String brandName,
                    String imageUrl,
                    Long likeCount
            ) {}
        }
    }
}
