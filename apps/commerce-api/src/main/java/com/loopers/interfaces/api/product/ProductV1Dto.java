package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductResult;
import com.loopers.domain.common.PageInfo;
import com.loopers.domain.product.ProductsInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;


public class ProductV1Dto {
    public static class Response {
        public record Detail(
                Long productId,
                String productName,
                Long price,
                String brandName,
                String imageUrl,
                Long likeCount,
                Long rank
        ) {
            public static Detail from(ProductResult productResult) {
                Long likeCount = productResult.getLikeCount() != null ? productResult.getLikeCount() : 0L;
                return new Detail(
                        productResult.getProductId(),
                        productResult.getProductName(),
                        productResult.getProductPrice(),
                        productResult.getBrandName(),
                        productResult.getImageUrl(),
                        likeCount,
                        productResult.getRank()
                );
            }
        }

        public record Summary(
                int totalPage,
                int page,
                int size,
                long totalItems,
                List<Item> items
        ) {

            public static Summary from(ProductsInfo productsInfo) {
                PageInfo pageInfo = productsInfo.getPageInfo();
                List<Item> item = productsInfo.getProductInfoList()
                        .stream()
                        .map(productInfo ->
                                Item.builder()
                                        .productId(productInfo.getProductId())
                                        .price(productInfo.getPrice())
                                        .productName(productInfo.getProductName())
                                        .brandName(productInfo.getBrandName())
                                        .imageUrl(productInfo.getImageUrl())
                                        .likeCount(productInfo.getLikeCount())
                                        .releasedAt(productInfo.getReleasedAt())
                                        .build()
                        ).toList();
                return new Summary(
                        pageInfo.totalPages(),
                        pageInfo.currentPage(),
                        pageInfo.pageSize(),
                        pageInfo.totalElements()
                        , item

                );
            }

            @Getter
            @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
            @Builder
            public static class Item {
                private final Long productId;
                private final Long price;
                private final String productName;
                private final String brandName;
                private final String imageUrl;
                private final Long likeCount;
                private final ZonedDateTime releasedAt;
            }
        }
    }
}
