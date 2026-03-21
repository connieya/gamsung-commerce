package com.loopers.infrastructure.product.sku;

import com.loopers.domain.product.sku.ProductSku;
import com.loopers.domain.product.sku.ProductSkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductSkuCoreRepository implements ProductSkuRepository {

    private final ProductSkuJpaRepository jpaRepository;

    @Override
    public ProductSku save(ProductSku sku) {
        return jpaRepository.save(sku);
    }

    @Override
    public Optional<ProductSku> findById(Long skuId) {
        return jpaRepository.findById(skuId);
    }

    @Override
    public List<ProductSku> findByProductId(Long productId) {
        return jpaRepository.findByProductId(productId);
    }

    @Override
    public boolean existsBySkuCode(String skuCode) {
        return jpaRepository.existsBySkuCode(skuCode);
    }
}
