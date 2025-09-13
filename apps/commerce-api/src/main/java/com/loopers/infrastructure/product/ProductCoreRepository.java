package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.brand.exception.BrandException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.brand.Brand;
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
public class ProductCoreRepository implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId).map(ProductEntity::toDomain);
    }

    @Override
    public Product save(Product product, Long brandId) {
        Brand brand = brandJpaRepository.findById(brandId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
        ProductEntity productEntity = ProductEntity.fromDomain(product, brand);
        return productJpaRepository.save(productEntity).toDomain();
    }

    @Override
    public List<Product> findByBrandId(Long brandId) {
        Brand brand = brandJpaRepository.findById(brandId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
        return productJpaRepository.findByBrand(brand)
                .stream().map(ProductEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public Page<ProductInfo> findProductDetails(Pageable pageable) {
        return productJpaRepository.findProductDetails(pageable);
    }

    @Override
    public Page<ProductInfo> findProductDetailsOptimized(Pageable pageable , Long brandId) {
        return productJpaRepository.findProductDetailsOptimized(pageable , brandId);
    }

    @Override
    public Page<ProductInfo> findProductDetailsDenormalizedLikeCount(Pageable pageable, Long brandId) {
        return productJpaRepository.findProductDetailsDenormalizedLikeCount(pageable , brandId);
    }


    @Override
    public Page<ProductInfo> findProductDetailsDenormalizedLikeCountOptimized(Pageable pageable, Long brandId) {
        return productJpaRepository.findProductDetailsDenormalizedLikeCountOptimized(pageable);
    }

    @Override
    public List<ProductInfo> findRankByIds(List<Long> rankingInfo) {
        return productJpaRepository.findRankByIds(rankingInfo);
    }

    @Override
    public Optional<ProductDetailInfo> findProductDetail(Long productId) {
        return productJpaRepository.findProductDetail(productId);
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return productJpaRepository.findAllById(productIds)
                .stream().map(ProductEntity::toDomain).collect(Collectors.toList());
    }


}
