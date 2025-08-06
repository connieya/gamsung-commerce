package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductDetailInfo;


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
}
