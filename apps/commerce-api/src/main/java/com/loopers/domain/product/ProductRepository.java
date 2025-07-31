package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long productId);

    Product save(Product product , Long brandId);

    Page<ProductInfo> findProductDetails(Pageable pageable);
}
