package com.loopers.domain.product;

import com.loopers.domain.common.PageInfo;
import com.loopers.domain.common.Sort;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

public class ProductsInfo {
    private List<ProductInfo> productInfoList;
    private PageInfo pageInfo;
    private Sort sort;

    @Builder
    private ProductsInfo(List<ProductInfo> productInfoList, PageInfo pageInfo, Sort sort) {
        this.productInfoList = productInfoList;
        this.pageInfo = pageInfo;
        this.sort = sort;
    }

    public static ProductsInfo create(Page<ProductInfo> productInfoPage, Sort sort) {
        List<ProductInfo> productInfos = productInfoPage.getContent();

        return ProductsInfo
                .builder()
                .pageInfo(PageInfo.create(
                        productInfoPage.getNumber()
                        ,productInfoPage.getSize()
                        ,productInfoPage.getTotalPages()
                        ,productInfoPage.getTotalElements()
                        ,productInfoPage.hasNext()))
                .build();
    }
}
