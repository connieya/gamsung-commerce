package com.loopers.infrastructure.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Brand;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "brand")
public class BrandEntity extends BaseEntity {

    private String name;
    private String description;

    public static BrandEntity fromDomain(Brand brand) {
        BrandEntity brandEntity = new BrandEntity();

        brandEntity.name = brand.getName();
        brandEntity.description = brand.getDescription();

        return brandEntity;
    }
}
