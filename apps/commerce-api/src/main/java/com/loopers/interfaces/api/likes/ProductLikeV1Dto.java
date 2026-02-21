package com.loopers.interfaces.api.likes;

import com.loopers.application.likes.GetLikeProductResult;
import com.loopers.domain.product.ProductDetailInfo;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

public class ProductLikeV1Dto {
    public static class Response {
        public record LikedProducts(
                List<LikedProduct> likedProducts
        ) {
            public static LikedProducts from(GetLikeProductResult getLikeProductResult) {
                List<ProductDetailInfo> productDetailInfos = getLikeProductResult.getProductDetailInfos();
                List<LikedProduct> likedProducts = productDetailInfos.stream()
                        .map(productDetailInfo ->
                                LikedProduct
                                        .builder()
                                        .productId(productDetailInfo.getProductId())
                                        .productPrice(productDetailInfo.getProductPrice())
                                        .productName(productDetailInfo.getProductName())
                                        .brandName(productDetailInfo.getBrandName())
                                        .imageUrl(productDetailInfo.getImageUrl())
                                        .likeCount(productDetailInfo.getLikeCount())
                                        .build()
                        ).toList();
                return new LikedProducts(likedProducts);
            }

            @Getter
            @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
            @Builder
            public static class LikedProduct {
                private final Long productId;
                private final String productName;
                private final Long productPrice;
                private final String brandName;
                private final String imageUrl;
                private final Long likeCount;
            }
        }
    }
}
