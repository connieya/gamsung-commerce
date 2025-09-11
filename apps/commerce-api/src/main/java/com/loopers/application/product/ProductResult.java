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
    private Long likeCount;

    @Builder
    private ProductResult(Long productId, String productName, Long productPrice, String brandName, Long brandId, Long likeCount) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.brandName = brandName;
        this.brandId = brandId;
        this.likeCount = likeCount;
    }

    public static ProductResult of(Long productId, String productName, Long productPrice, String brandName, Long brandId, Long likeCount) {
        return ProductResult
                .builder()
                .productId(productId)
                .productName(productName)
                .productPrice(productPrice)
                .brandName(brandName)
                .brandId(brandId)
                .likeCount(likeCount)
                .build();
    }
}
