package com.loopers.interfaces.api.sku;

import com.loopers.application.sku.SkuResult;
import com.loopers.domain.product.option.OptionType;

import java.util.List;

public class SkuV1Dto {

    public static class Request {

        public record RegisterOption(OptionType optionType, String optionValue) {
        }

        public record RegisterSku(Long productId, String skuCode, Long additionalPrice, List<Long> optionIds) {
        }
    }

    public static class Response {

        public record Option(Long id, Long productId, OptionType optionType, String optionValue) {

            public static Option from(SkuResult.Option result) {
                return new Option(result.id(), result.productId(), result.optionType(), result.optionValue());
            }
        }

        public record Sku(Long id, Long productId, String skuCode, Long additionalPrice, List<SkuOption> options) {

            public record SkuOption(Long id, OptionType optionType, String optionValue) {
            }

            public static Sku from(SkuResult.Sku result) {
                List<SkuOption> options = result.options().stream()
                        .map(o -> new SkuOption(o.id(), o.optionType(), o.optionValue()))
                        .toList();
                return new Sku(result.id(), result.productId(), result.skuCode(), result.additionalPrice(), options);
            }
        }
    }
}
