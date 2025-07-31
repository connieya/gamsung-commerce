package com.loopers.domain.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductDetailInfo {

    private String productName;
    private Long productPrice;
    private String brandName;
    private Long likeCount;

    @Builder
    private ProductDetailInfo(String productName, Long productPrice, String brandName, Long likeCount) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.brandName = brandName;
        this.likeCount = likeCount;
    }

    public static ProductDetailInfo create(String productName , Long productPrice , String brandName , Long likeCount) {
        return ProductDetailInfo
                .builder()
                .productName(productName)
                .productPrice(productPrice)
                .brandName(brandName)
                .likeCount(likeCount)
                .build();
    }
}
