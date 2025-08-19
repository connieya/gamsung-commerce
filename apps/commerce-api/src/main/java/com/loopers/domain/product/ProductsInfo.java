package com.loopers.domain.product;

import com.loopers.domain.common.PageInfo;
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

    private static final Map<ProductSort, Comparator<ProductInfo>> comparatorMap = Map.of(
            ProductSort.PRICE_ASC, Comparator.comparing(ProductInfo::getPrice),
            ProductSort.LATEST_DESC, Comparator.comparing(ProductInfo::getReleasedAt).reversed(),
            ProductSort.LIKES_DESC, Comparator.comparing(ProductInfo::getLikeCount).reversed()
    );

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

    public static ProductsInfo create(Page<ProductInfo> productInfoPage , ProductSort productSort) {
        return ProductsInfo
                .builder()
                .pageInfo(PageInfo.create(
                        productInfoPage.getNumber()
                        , productInfoPage.getSize()
                        , productInfoPage.getTotalPages()
                        , productInfoPage.getTotalElements()
                        , productInfoPage.hasNext()))
                .productInfoList(applySort(productInfoPage.getContent(), productSort))
                .build();
    }

    private static List<ProductInfo> applySort(List<ProductInfo> productInfos, ProductSort productSort) {
        Comparator<ProductInfo> comparator = comparatorMap.getOrDefault(productSort, Comparator.comparing(ProductInfo::getProductId));
        return productInfos.stream().sorted(comparator).toList();

    }

}
