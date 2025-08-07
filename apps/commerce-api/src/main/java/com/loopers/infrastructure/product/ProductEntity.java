package com.loopers.infrastructure.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import com.loopers.infrastructure.brand.BrandEntity;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "product")
public class ProductEntity extends BaseEntity {

    private String name;

    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_brand_id")
    private BrandEntity brandEntity;

    private ZonedDateTime releasedAt;

    public static ProductEntity fromDomain(Product product , BrandEntity brandEntity) {
        ProductEntity productEntity = new ProductEntity();

        productEntity.name = product.getName();
        productEntity.price = product.getPrice();
        productEntity.brandEntity = brandEntity;
        productEntity.releasedAt = product.getReleasedAt();

        return productEntity;
    }

    public Product toDomain() {
        return Product.builder()
                .id(id)
                .name(name)
                .price(price)
                .brandId(brandEntity.getId())
                .releasedAt(releasedAt)
                .build();
    }

}
