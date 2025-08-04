package com.loopers.infrastructure.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "brand")
@Getter
public class BrandEntity extends BaseEntity {

    private String name;
    private String description;

    public static BrandEntity fromDomain(Brand brand) {
        BrandEntity brandEntity = new BrandEntity();

        brandEntity.name = brand.getName();
        brandEntity.description = brand.getDescription();

        return brandEntity;
    }

    public Brand toDomain() {
        return Brand.builder()
                .id(id)
                .name(name)
                .description(description)
                .build();
    }
}
