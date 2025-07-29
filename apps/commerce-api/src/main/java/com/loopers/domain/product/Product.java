package com.loopers.domain.product;

import com.loopers.domain.product.brand.Brand;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

    private String name;
    private Long price;
    private Brand brand;


    @Builder
    private Product(String name, Long price, Brand brand) {
        this.name = name;
        this.price = price;
        this.brand = brand;
    }

    public static Product create(String name, Long price, Brand brand) {;
        return Product.builder()
                .name(name)
                .price(price)
                .brand(brand)
                .build();
    }
}
