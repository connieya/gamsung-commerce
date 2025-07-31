package com.loopers.domain.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCommand {

    private String name;
    private Long price;
    private Long brandId;

    @Builder
    private ProductCommand(String name, Long price, Long brandId) {
        this.name = name;
        this.price = price;
        this.brandId = brandId;
    }

    public static ProductCommand of(String name, Long price, Long brandId) {
        return ProductCommand
                .builder()
                .name(name)
                .price(price)
                .brandId(brandId)
                .build();
    }
}
