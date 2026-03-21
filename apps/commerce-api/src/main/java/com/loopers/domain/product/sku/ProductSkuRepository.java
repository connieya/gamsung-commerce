package com.loopers.domain.product.sku;

import java.util.List;
import java.util.Optional;

public interface ProductSkuRepository {
    ProductSku save(ProductSku sku);

    Optional<ProductSku> findById(Long skuId);

    List<ProductSku> findByProductId(Long productId);

    boolean existsBySkuCode(String skuCode);
}
