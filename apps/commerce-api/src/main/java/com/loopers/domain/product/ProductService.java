package com.loopers.domain.product;

import com.loopers.domain.activity.event.ActivityEvent;
import com.loopers.domain.brand.BrandCacheRepository;
import com.loopers.domain.brand.exception.BrandException;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.Brand;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final BrandRepository brandRepository;
    private final ProductLikeRepository productLikeRepository;
    private final BrandCacheRepository brandCacheRepository;
    private final ProductCacheRepository productCacheRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void register(ProductCommand.Register register) {
        Product product = Product.create(
                register.getName()
                , register.getPrice()
                , register.getBrandId()
                , ZonedDateTime.now()
        );
        productRepository.save(product, register.getBrandId());
    }

    @Transactional(readOnly = true)
    public ProductDetailInfo getProduct(Long productId) {
        Optional<ProductDetailInfo> productDetailById = productCacheRepository.findProductDetailById(productId);
        if (productDetailById.isPresent()) {
            return productDetailById.get();
        }

        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));
        Long brandId = product.getBrandId();
        Brand brand = brandCacheRepository.findById(brandId)
                .orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));

        Long likeCount = productLikeRepository.getLikeCount(productId);


        ProductDetailInfo productDetailInfo = ProductDetailInfo.create(product.getId(), product.getName(), product.getPrice(), brand.getName(), likeCount);
        productCacheRepository.saveProductDetail(productId, productDetailInfo);

        applicationEventPublisher.publishEvent(ActivityEvent.View.from(productId));

        return productDetailInfo;

    }


    @Transactional(readOnly = true)
    public ProductDetailInfo getProduct_Old(Long productId) { // 공부용으로 남겨둠
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));
        Long brandId = product.getBrandId();

        Brand brand = brandCacheRepository.findById(brandId)
                .orElseGet(() -> {
                    Brand brandFromDb = brandRepository.findBrand(brandId)
                            .orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
                    brandCacheRepository.save(brandFromDb);
                    return brandFromDb;
                });
        Long likeCount = productLikeRepository.getLikeCount(productId);

        return ProductDetailInfo.create(product.getId(), product.getName(), product.getPrice(), brand.getName(), likeCount);
    }

    @Transactional(readOnly = true) // 좋아요 비정규화 하기 전 (product_like 테이블과 조인 )
    public ProductsInfo getProducts(int size, int page, ProductSort sortType) {
        Pageable pageable = PageRequest.of(page, size, sortType.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetails(pageable);
        return ProductsInfo.create(productDetails);
    }

    @Transactional(readOnly = true) // 좋아요 비정규화 하기 전 (product_like 테이블과 조인 )
    public ProductsInfo getProducts_Old(int size, int page, ProductSort sortType) {
        Pageable pageable = PageRequest.of(page, size, sortType.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetails(pageable);
        return ProductsInfo.create(productDetails, sortType);
    }

    @Transactional(readOnly = true) // 좋아요 비정규화 하기 전 쿼리 최적화 (product_like count 서브 쿼리 )
    public ProductsInfo getProductsOptimized(ProductCommand.Search search) {
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize(), search.getProductSort().toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsOptimized(pageable, search.getBrandId());
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


    public List<Product> findAllById(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }


}
