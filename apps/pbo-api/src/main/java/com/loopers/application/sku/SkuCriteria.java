package com.loopers.application.sku;

import com.loopers.domain.product.option.OptionType;

import java.util.List;

public class SkuCriteria {

    public record RegisterOption(Long productId, OptionType optionType, String optionValue) {
    }

    public record RegisterSku(Long productId, String skuCode, Long additionalPrice, List<Long> optionIds) {
    }
}
