package com.loopers.interfaces.api.rank;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.rank.RankingInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

public class RankingV1Dto {
    public record SummaryResponse(
            List<Item> items
    ) {

        public static SummaryResponse from(RankingInfo rankingInfo) {
            List<ProductInfo> productInfos = rankingInfo.getProductInfos();
            return new SummaryResponse(
                    productInfos.stream()
                            .map(productInfo ->
                                    Item
                                            .builder()
                                            .productId(productInfo.getProductId())
                                            .productName(productInfo.getProductName())
                                            .brandName(productInfo.getBrandName())
                                            .likeCount(productInfo.getLikeCount())
                                            .releasedAt(productInfo.getReleasedAt())
                                            .price(productInfo.getPrice())
                                            .build()
                            ).toList()
            );
        }
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class Item {
        private final Long productId;
        private final Long price;
        private final String productName;
        private final String brandName;
        private final Long likeCount;
        private final ZonedDateTime releasedAt;
    }
}
