package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductCoreRepository implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    @Override
    public boolean existsById(Long productId) {
        return jpaRepository.existsById(productId);
    }
}
