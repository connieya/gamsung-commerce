package com.loopers.infrastructure.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "product")
public class ProductEntity extends BaseEntity {

    private String name;
    private String description;
    private Long price;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_brand_id")
    private BrandEntity brandEntity;

    public static ProductEntity fromDomain(Product product) {
        ProductEntity productEntity = new ProductEntity();

        productEntity.name = product.getName();
        productEntity.description = product.getDescription();
        productEntity.price = product.getPrice();
        productEntity.brandEntity = BrandEntity.fromDomain(product.getBrand());

        return productEntity;
    }



}
