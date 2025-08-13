package com.loopers.infrastructure.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import com.loopers.domain.brand.Brand;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "product")
public class ProductEntity extends BaseEntity {

    private String name;

    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_brand_id")
    private Brand brand;

    private ZonedDateTime releasedAt;

    public static ProductEntity fromDomain(Product product , Brand brand) {
        ProductEntity productEntity = new ProductEntity();

        productEntity.name = product.getName();
        productEntity.price = product.getPrice();
        productEntity.brand = brand;
        productEntity.releasedAt = product.getReleasedAt();

        return productEntity;
    }

    public Product toDomain() {
        return Product.builder()
                .id(id)
                .name(name)
                .price(price)
                .brandId(brand.getId())
                .releasedAt(releasedAt)
                .build();
    }

}
