package com.loopers.domain.rank;

import com.loopers.domain.product.ProductInfo;
import lombok.Getter;

import java.util.List;

@Getter
public class RankingInfo {
    private List<ProductInfo> productInfos;

    public RankingInfo(List<ProductInfo> productInfos) {
        this.productInfos = productInfos;
    }

    public static RankingInfo from(List<ProductInfo> productInfos) {
        return new RankingInfo(productInfos);
    }
}
