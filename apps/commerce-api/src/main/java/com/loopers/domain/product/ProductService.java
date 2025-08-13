package com.loopers.domain.product;

import com.loopers.domain.brand.exception.BrandException;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.Brand;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public void register(ProductCommand productCommand) {
        Product product = Product.create(
                productCommand.getName()
                , productCommand.getPrice()
                , productCommand.getBrandId()
                , ZonedDateTime.now()
        );
        productRepository.save(product, productCommand.getBrandId());
    }

    @Transactional(readOnly = true)
    public ProductDetailInfo getProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));
        Brand brand = brandRepository.findBrand(productId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
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
    public ProductsInfo getProductsOptimized(int size, int page, ProductSort sortType) {
        Pageable pageable = PageRequest.of(page, size, sortType.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsOptimized(pageable);
        return ProductsInfo.create(productDetails);
    }

    public List<Product> findAllById(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }


}
