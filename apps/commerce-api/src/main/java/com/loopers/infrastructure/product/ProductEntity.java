package com.loopers.infrastructure.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import com.loopers.infrastructure.product.brand.BrandEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class ProductEntity extends BaseEntity {

    private String name;

    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_brand_id")
    private BrandEntity brandEntity;

    public static ProductEntity fromDomain(Product product) {
        ProductEntity productEntity = new ProductEntity();

        productEntity.name = product.getName();
        productEntity.price = product.getPrice();
        productEntity.brandEntity = BrandEntity.fromDomain(product.getBrand());

        return productEntity;
    }

    public Product toDomain() {
        return Product.builder()
                .id(id)
                .name(name)
                .price(price)
                .brand(brandEntity.toDomain())
                .build();
    }

}
