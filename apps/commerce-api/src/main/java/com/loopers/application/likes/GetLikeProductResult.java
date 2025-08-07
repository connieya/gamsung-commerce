package com.loopers.application.likes;

import com.loopers.domain.product.ProductDetailInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class GetLikeProductResult {
    private List<ProductDetailInfo> productDetailInfos;


    private GetLikeProductResult(List<ProductDetailInfo> productDetailInfos) {
        this.productDetailInfos = productDetailInfos;
    }

    public static GetLikeProductResult create(List<ProductDetailInfo> productDetailInfos) {
        return new GetLikeProductResult(productDetailInfos);
    }

}
