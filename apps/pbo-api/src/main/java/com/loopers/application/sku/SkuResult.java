package com.loopers.application.sku;

import com.loopers.domain.product.option.OptionType;
import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.sku.ProductSku;

import java.util.List;

public class SkuResult {

    public record Option(Long id, Long productId, OptionType optionType, String optionValue) {

        public static Option from(ProductOption option) {
            return new Option(
                    option.getId(),
                    option.getProductId(),
                    option.getOptionType(),
                    option.getOptionValue()
            );
        }
    }

    public record Sku(Long id, Long productId, String skuCode, Long additionalPrice, List<SkuOption> options) {

        public record SkuOption(Long id, OptionType optionType, String optionValue) {
        }

        public static Sku from(ProductSku sku, List<ProductOption> options) {
            List<SkuOption> skuOptions = options.stream()
                    .map(o -> new SkuOption(o.getId(), o.getOptionType(), o.getOptionValue()))
                    .toList();
            return new Sku(
                    sku.getId(),
                    sku.getProductId(),
                    sku.getSkuCode(),
                    sku.getAdditionalPrice(),
                    skuOptions
            );
        }
    }
}
