package com.loopers.domain.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductDetailInfo {

    public static final ProductDetailInfo EMPTY = ProductDetailInfo.builder().build();

    private Long productId;
    private String productName;
    private Long productPrice;
    private String brandName;
    private Long brandId;
    private Long likeCount;

    @Builder
    public ProductDetailInfo(Long productId, String productName, Long productPrice, String brandName, Long brandId, Long likeCount) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.brandName = brandName;
        this.brandId = brandId;
        this.likeCount = likeCount;
    }

    public static ProductDetailInfo create(Long productId, String productName, Long productPrice, String brandName, Long brandId, Long likeCount) {
        return ProductDetailInfo
                .builder()
                .productId(productId)
                .productName(productName)
                .productPrice(productPrice)
                .brandName(brandName)
                .likeCount(likeCount)
                .brandId(brandId)
                .build();
    }
}
