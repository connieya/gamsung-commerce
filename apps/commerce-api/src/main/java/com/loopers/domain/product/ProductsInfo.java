package com.loopers.domain.product;

import com.loopers.domain.common.PageInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class ProductsInfo {
    private List<ProductInfo> productInfoList;
    private PageInfo pageInfo;

    @Builder
    private ProductsInfo(List<ProductInfo> productInfoList, PageInfo pageInfo) {
        this.productInfoList = productInfoList;
        this.pageInfo = pageInfo;

    }

    public static ProductsInfo create(Page<ProductInfo> productInfoPage) {
        return ProductsInfo
                .builder()
                .pageInfo(PageInfo.create(
                        productInfoPage.getNumber()
                        , productInfoPage.getSize()
                        , productInfoPage.getTotalPages()
                        , productInfoPage.getTotalElements()
                        , productInfoPage.hasNext()))
                .productInfoList(productInfoPage.getContent())
                .build();
    }

}
