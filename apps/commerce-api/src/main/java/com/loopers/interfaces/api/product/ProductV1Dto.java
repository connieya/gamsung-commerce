package com.loopers.interfaces.api.product;

import com.loopers.domain.common.PageInfo;
import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductsInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;



public class ProductV1Dto {
    public record DetailResponse(
            Long productId,
            String productName,
            Long price,
            String brandName,
            Long likeCount
    ) {
        public static DetailResponse from(ProductDetailInfo productDetailInfo) {
            return new DetailResponse(
                    productDetailInfo.getProductId()
                    , productDetailInfo.getProductName()
                    , productDetailInfo.getProductPrice()
                    , productDetailInfo.getBrandName()
                    , productDetailInfo.getLikeCount()
            );
        }
    }

    public record SummaryResponse(
            int totalPage,
            int page,
            int size,
            long totalItems,
            List<Item> items
    ) {

        public static SummaryResponse from(ProductsInfo productsInfo) {
            PageInfo pageInfo = productsInfo.getPageInfo();
            List<Item> item = productsInfo.getProductInfoList()
                    .stream()
                    .map(productInfo ->
                            Item.builder()
                                    .productId(productInfo.getProductId())
                                    .price(productInfo.getPrice())
                                    .productName(productInfo.getProductName())
                                    .brandName(productInfo.getBrandName())
                                    .likeCount(productInfo.getLikeCount())
                                    .releasedAt(productInfo.getReleasedAt())
                                    .build()
                    ).toList();
            return new SummaryResponse(
                    pageInfo.totalPages(),
                    pageInfo.currentPage(),
                    pageInfo.pageSize(),
                    pageInfo.totalElements()
                    , item

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
