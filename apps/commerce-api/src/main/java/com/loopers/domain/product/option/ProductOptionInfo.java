package com.loopers.domain.product.option;

public record ProductOptionInfo(Long id, Long productId, OptionType optionType, String optionValue) {

    public static ProductOptionInfo from(ProductOption option) {
        return new ProductOptionInfo(
                option.getId(),
                option.getProductId(),
                option.getOptionType(),
                option.getOptionValue()
        );
    }
}
