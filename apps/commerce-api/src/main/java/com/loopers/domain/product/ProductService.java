package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.exception.BrandException;
import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTargetType;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCacheRepository productCacheRepository;
    private final LikeSummaryRepository likeSummaryRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public Product register(ProductCommand.Register register) {
        Brand brand = brandRepository.findBrand(register.getBrandId())
                .orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));

        Product product = Product.create(
                register.getName(),
                register.getPrice(),
                brand,
                register.getCategoryId(),
                null,
                ZonedDateTime.now()
        );
        Product save = productRepository.save(product);
        likeSummaryRepository.save(LikeSummary.create(save.getId(), LikeTargetType.PRODUCT));
        return save;
    }


    @Transactional
    public ProductDetailInfo getProductDetail(Long productId) {
        Optional<ProductDetailInfo> productDetailById = productCacheRepository.findProductDetailById(productId);
        if (productDetailById.isPresent()) {
            return productDetailById.get();
        }
        ProductDetailInfo productDetailInfo = productRepository.findProductDetail(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));
        productCacheRepository.saveProductDetail(productDetailInfo);
        return productDetailInfo;
    }

    @Transactional(readOnly = true) // 좋아요 비정규화 하기 전 (product_like 테이블과 조인 )
    public ProductsInfo getProducts(int size, int page, ProductSort sortType) {
        Pageable pageable = PageRequest.of(page, size, sortType.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetails(pageable);
        return ProductsInfo.create(productDetails);
    }

    @Transactional(readOnly = true) // 좋아요 비정규화 하기 전 쿼리 최적화 (product_like count 서브 쿼리 )
    public ProductsInfo getProductsOptimized(int size, int page, ProductSort sortType) {
        Pageable pageable = PageRequest.of(page, size, sortType.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsOptimized(pageable);
        return ProductsInfo.create(productDetails);
    }

    @Transactional(readOnly = true) // 좋아요 비정규화 적용 (product_summary 테이블 좋아요 count )
    public ProductsInfo getProductsDenormalizedLikeCount(ProductCommand.Search search) {
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize(), search.getProductSort().toSort());
        Page<ProductInfo> page = productCacheRepository.findProductDetails(pageable, search.getBrandId());

        // 캐시 Hit
        if (page.hasContent()) {
            return ProductsInfo.create(page);
        }

        // 캐시 Miss
        Page<ProductInfo> productDetails = productRepository.findProductDetailsDenormalizedLikeCount(pageable, search.getBrandId());
        productCacheRepository.save(search.getBrandId(), productDetails);
        return ProductsInfo.create(productDetails);
    }


    @Transactional(readOnly = true) // 좋아요 비정규화 적용 (brandId 없이, 캐시 미적용)
    public ProductsInfo getProductsDenormalized(int size, int page, ProductSort sortType) {
        Pageable pageable = PageRequest.of(page, size, sortType.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsDenormalizedLikeCountOptimized(pageable);
        return ProductsInfo.create(productDetails);
    }

    @Transactional(readOnly = true)
    public ProductsInfo getProductsByCategoryId(Long categoryId, int page, int size, ProductSort sortType) {
        Pageable pageable = PageRequest.of(page, size, sortType.toSort());
        Page<ProductInfo> productDetails = productRepository.findByCategoryId(pageable, categoryId);
        return ProductsInfo.create(productDetails);
    }

    public List<Product> findAllById(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }


}
