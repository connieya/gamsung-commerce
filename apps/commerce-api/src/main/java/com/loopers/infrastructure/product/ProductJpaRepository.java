package com.loopers.infrastructure.product;

import com.loopers.infrastructure.product.brand.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductEntity,Long> {

    List<ProductEntity> findByBrandEntity(BrandEntity brandEntity);
}
