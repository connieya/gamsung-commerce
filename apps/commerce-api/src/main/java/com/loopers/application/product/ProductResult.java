package com.loopers.application.product;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProductResult {
    private Long productId;
    private String productName;
    private Long productPrice;
    private String brandName;
    private Long brandId;
    private String imageUrl;
    private Long likeCount;
    private Long rank;

    @Builder
    private ProductResult(Long productId, String productName, Long productPrice, String brandName, Long brandId, String imageUrl, Long likeCount, Long rank) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.brandName = brandName;
        this.brandId = brandId;
        this.imageUrl = imageUrl;
        this.likeCount = likeCount;
        this.rank = rank;
    }

    public static ProductResult of(Long productId, String productName, Long productPrice, String brandName, Long brandId, String imageUrl, Long likeCount, Long rank) {
        return ProductResult
                .builder()
                .productId(productId)
                .productName(productName)
                .productPrice(productPrice)
                .brandName(brandName)
                .brandId(brandId)
                .imageUrl(imageUrl)
                .likeCount(likeCount)
                .rank(rank)
                .build();
    }
}
