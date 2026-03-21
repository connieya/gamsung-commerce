package com.loopers.infrastructure.product.option;

import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.option.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductOptionCoreRepository implements ProductOptionRepository {

    private final ProductOptionJpaRepository jpaRepository;

    @Override
    public ProductOption save(ProductOption option) {
        return jpaRepository.save(option);
    }

    @Override
    public Optional<ProductOption> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ProductOption> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public List<ProductOption> findAllByIdIn(List<Long> ids) {
        return jpaRepository.findAllById(ids);
    }
}
