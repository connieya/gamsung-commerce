package com.loopers.domain.product;

import com.loopers.domain.common.PageInfo;
import com.loopers.domain.common.Sort;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Getter
public class ProductsInfo {
    private List<ProductInfo> productInfoList;
    private PageInfo pageInfo;

    private static final Map<Sort, Comparator<ProductInfo>> comparatorMap = Map.of(
            Sort.PRICE_ASC, Comparator.comparing(ProductInfo::getPrice),
            Sort.LATEST, Comparator.comparing(ProductInfo::getReleasedAt).reversed(),
            Sort.LIKES_DESC, Comparator.comparing(ProductInfo::getLikeCount).reversed()
    );

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
        Comparator<ProductInfo> comparator = comparatorMap.getOrDefault(sort, Comparator.comparing(ProductInfo::getProductId));
        return productInfos.stream().sorted(comparator).toList();

    }

}
