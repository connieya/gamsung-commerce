package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.exception.BrandException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.brand.BrandEntity;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId).map(ProductEntity::toDomain);
    }

    @Override
    public Product save(Product product, Long brandId) {
        BrandEntity brandEntity = brandJpaRepository.findById(brandId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
        ProductEntity productEntity = ProductEntity.fromDomain(product, brandEntity);
        return productJpaRepository.save(productEntity).toDomain();
    }

    @Override
    public List<Product> findByBrandId(Long brandId) {
        BrandEntity brandEntity = brandJpaRepository.findById(brandId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
        return productJpaRepository.findByBrandEntity(brandEntity)
                .stream().map(ProductEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<ProductInfo> findProductDetails(Pageable pageable) {
        return productJpaRepository.findProductDetails(pageable);
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return productJpaRepository.findAllById(productIds)
                .stream().map(ProductEntity::toDomain).collect(Collectors.toList());
    }
}
