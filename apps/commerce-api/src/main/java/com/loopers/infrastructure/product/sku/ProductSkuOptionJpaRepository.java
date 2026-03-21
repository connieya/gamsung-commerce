package com.loopers.infrastructure.product.sku;

import com.loopers.domain.product.sku.ProductSkuOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductSkuOptionJpaRepository extends JpaRepository<ProductSkuOption, Long> {
}
