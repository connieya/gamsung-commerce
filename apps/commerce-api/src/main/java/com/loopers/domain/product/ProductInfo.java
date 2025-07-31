package com.loopers.domain.product;

import java.time.ZonedDateTime;

public class ProductInfo {
    private Long productId;
    private Long price;
    private String productName;
    private String brandName;
    private Long likeCount;
    private ZonedDateTime releasedAt;


    public ProductInfo(Long productId, Long price, String productName, String brandName, Long likeCount, ZonedDateTime releasedAt) {
        this.productId = productId;
        this.price = price;
        this.productName = productName;
        this.brandName = brandName;
        this.likeCount = likeCount;
        this.releasedAt = releasedAt;
    }
}
