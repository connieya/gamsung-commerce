package com.loopers.domain.product;

import com.loopers.domain.product.brand.Brand;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

    private Long id;
    private String name;
    private Long price;
    private Long brandId;
    private ZonedDateTime releasedAt;

    @Builder
    private Product(Long id, String name, Long price, Long brandId ,ZonedDateTime releasedAt) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.brandId = brandId;
        this.releasedAt= releasedAt;
    }

    public static Product create(String name, Long price, Long brandId , ZonedDateTime releasedAt) {
        return Product.builder()
                .name(name)
                .price(price)
                .brandId(brandId)
                .releasedAt(releasedAt)
                .build();
    }
}
