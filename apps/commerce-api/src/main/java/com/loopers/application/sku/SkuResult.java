package com.loopers.application.sku;

import com.loopers.domain.product.option.OptionType;
import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.sku.ProductSku;

import java.util.List;
import java.util.function.Function;

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

    public record OptionList(List<Option> options) {

        public static OptionList from(List<ProductOption> options) {
            return new OptionList(options.stream().map(Option::from).toList());
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

    public record SkuList(List<Sku> skus) {

        public static SkuList from(List<ProductSku> skuList, Function<ProductSku, List<ProductOption>> optionLoader) {
            return new SkuList(skuList.stream()
                    .map(sku -> Sku.from(sku, optionLoader.apply(sku)))
                    .toList());
        }
    }
}
