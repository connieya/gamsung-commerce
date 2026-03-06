package com.loopers.domain.product.fixture;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ProductFixture {

    private String name = "테스트 상품";
    private Long price = 10000L;
    private Brand brand;
    private Long categoryId = 1L;
    private String imageUrl = null;
    private ZonedDateTime releasedAt = ZonedDateTime.now(ZoneId.systemDefault());

    public static ProductFixture create() {
        return new ProductFixture();
    }

    public ProductFixture name(String name) {
        this.name = name;
        return this;
    }

    public ProductFixture price(Long price) {
        this.price = price;
        return this;
    }

    public ProductFixture brand(Brand brand) {
        this.brand = brand;
        return this;
    }

    public ProductFixture categoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public ProductFixture imageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public ProductFixture releasedAt(ZonedDateTime releasedAt) {
        this.releasedAt = releasedAt;
        return this;
    }

    public Product build() {
        return Product.create(name, price, brand, categoryId, imageUrl, releasedAt);
    }
}
