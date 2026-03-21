package com.loopers.domain.product.sku;

import java.util.List;

public record ProductSkuInfo(Long id, Long productId, String skuCode, Long additionalPrice, List<Long> optionIds) {

    public static ProductSkuInfo from(ProductSku sku) {
        List<Long> optionIds = sku.getSkuOptions().stream()
                .map(ProductSkuOption::getOptionId)
                .toList();
        return new ProductSkuInfo(
                sku.getId(),
                sku.getProductId(),
                sku.getSkuCode(),
                sku.getAdditionalPrice(),
                optionIds
        );
    }
}
