package com.loopers.domain.product.sku;

import java.util.List;

public class ProductSkuCommand {

    public record Create(Long productId, String skuCode, Long additionalPrice, List<Long> optionIds) {
    }
}
