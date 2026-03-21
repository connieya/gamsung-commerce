package com.loopers.domain.product.option;

public class ProductOptionCommand {

    public record Create(Long productId, OptionType optionType, String optionValue) {
    }
}
