package com.loopers.domain.product.option;

import java.util.List;
import java.util.Optional;

public interface ProductOptionRepository {
    ProductOption save(ProductOption option);

    Optional<ProductOption> findById(Long id);

    List<ProductOption> findByProductId(Long productId);

    List<ProductOption> findAllByIdIn(List<Long> ids);
}
