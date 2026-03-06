package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.exception.BrandException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductCoreRepository implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<Product> findByBrandId(Long brandId) {
        Brand brand = brandJpaRepository.findById(brandId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
        return productJpaRepository.findByBrand(brand);
    }

    @Override
    public Page<ProductInfo> findProductDetails(Pageable pageable) {
        return productJpaRepository.findProductDetails(pageable);
    }

    @Override
    public Page<ProductInfo> findProductDetailsOptimized(Pageable pageable) {
        return productJpaRepository.findProductDetailsOptimized(pageable);
    }

    @Override
    public Page<ProductInfo> findProductDetailsDenormalizedLikeCount(Pageable pageable, Long brandId) {
        return productJpaRepository.findProductDetailsDenormalizedLikeCount(pageable, brandId);
    }

    @Override
    public Page<ProductInfo> findProductDetailsDenormalizedLikeCountOptimized(Pageable pageable, Long brandId) {
        return productJpaRepository.findProductDetailsDenormalizedLikeCountOptimized(pageable);
    }

    @Override
    public Page<ProductInfo> findProductDetailsDenormalizedLikeCountOptimized(Pageable pageable) {
        return productJpaRepository.findProductDetailsDenormalizedLikeCountOptimized(pageable);
    }

    @Override
    public Page<ProductInfo> findByCategoryId(Pageable pageable, Long categoryId) {
        return productJpaRepository.findByCategoryId(pageable, categoryId);
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
        return productJpaRepository.findAllById(productIds);
    }
}
