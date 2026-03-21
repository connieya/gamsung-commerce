package com.loopers.infrastructure.product.sku;

import com.loopers.domain.product.sku.ProductSku;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSkuJpaRepository extends JpaRepository<ProductSku, Long> {

    @EntityGraph(attributePaths = {"skuOptions"})
    List<ProductSku> findByProductId(Long productId);

    boolean existsBySkuCode(String skuCode);
}
