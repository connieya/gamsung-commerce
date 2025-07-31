package com.loopers.domain.product;

import com.loopers.domain.common.PageInfo;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

public class ProductsInfo {
    private List<ProductInfo> productInfoList;
    private PageInfo pageInfo;
    private String sort;

    @Builder
    private ProductsInfo(List<ProductInfo> productInfoList, PageInfo pageInfo, String sort) {
        this.productInfoList = productInfoList;
        this.pageInfo = pageInfo;
        this.sort = sort;
    }

    public static ProductsInfo create(Page<ProductInfo> productInfoPage ,String sort) {
        return null;
    }
}
