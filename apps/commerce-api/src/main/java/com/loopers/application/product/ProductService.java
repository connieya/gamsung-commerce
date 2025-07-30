package com.loopers.application.product;

import com.loopers.application.product.exception.BrandException;
import com.loopers.application.product.exception.ProductException;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.brand.Brand;
import com.loopers.domain.product.brand.BrandRepository;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductLikeRepository productLikeRepository;

    public void register(ProductCommand productCommand){
        Product product = Product.create(productCommand.getName(), productCommand.getPrice(), productCommand.getBrandId());
        productRepository.save(product , productCommand.getBrandId());
    }

    public ProductResult getProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));
        Brand brand = brandRepository.findBrand(productId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
        Long likeCount = productLikeRepository.getLikeCount(productId);

        return ProductResult.create(product.getName(), product.getPrice(), brand.getName(), likeCount);
    }
}
