package com.loopers.domain.product;

import com.loopers.domain.common.Sort;
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

    @Transactional(readOnly = true)
    public ProductsInfo getProducts(int size, int page, Sort sort) {
        Pageable pageable = PageRequest.of(size, page);
        Page<ProductInfo> productDetails = productRepository.findProductDetails(pageable);
        return ProductsInfo.create(productDetails, sort);
    }

    public List<Product> findAllById(List<Long> productIds) {
        return productRepository.findAllById(productIds);
    }
}
