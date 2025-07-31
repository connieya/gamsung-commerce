package com.loopers.domain.product;

import com.loopers.domain.common.PageInfo;
import com.loopers.domain.common.Sort;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.Comparator;
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

    public static ProductsInfo create(Page<ProductInfo> productInfoPage, Sort sort) {
        return ProductsInfo
                .builder()
                .pageInfo(PageInfo.create(
                        productInfoPage.getNumber()
                        , productInfoPage.getSize()
                        , productInfoPage.getTotalPages()
                        , productInfoPage.getTotalElements()
                        , productInfoPage.hasNext()))
                .productInfoList(applySort(productInfoPage.getContent(), sort))
                .build();
    }

    private static List<ProductInfo> applySort(List<ProductInfo> productInfos, Sort sort) {
        if (sort == Sort.PRICE_ASC) {
            return productInfos.stream()
                    .sorted(Comparator.comparing(ProductInfo::getPrice))
                    .toList();
        }

        if (sort == Sort.LATEST) {
            return productInfos.stream()
                    .sorted(Comparator.comparing(ProductInfo::getReleasedAt).reversed())
                    .toList();
        }

        if (sort == Sort.LIKES_DESC) {
            return productInfos.stream()
                    .sorted(Comparator.comparing(ProductInfo::getLikeCount).reversed())
                    .toList();
        }
        return productInfos;
    }

}
