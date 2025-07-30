package com.loopers.domain.product;

import com.loopers.domain.product.exception.BrandException;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.brand.Brand;
import com.loopers.domain.product.brand.BrandRepository;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public void register(ProductCommand productCommand){
        Product product = Product.create(productCommand.getName(), productCommand.getPrice(), productCommand.getBrandId());
        productRepository.save(product , productCommand.getBrandId());
    }

    @Transactional(readOnly = true)
    public ProductResult getProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));
        Brand brand = brandRepository.findBrand(productId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
        Long likeCount = productLikeRepository.getLikeCount(productId);

        return ProductResult.create(product.getName(), product.getPrice(), brand.getName(), likeCount);
    }

    @Transactional(readOnly = true)
    public void getProducts(int size , int page) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);
        List<Long> brandIds = products.stream()
                .map(Product::getBrandId)
                .distinct()
                .toList();
        List<Brand> brands = brandRepository.findAllById(brandIds);

        List<Long> productIds = products.stream()
                .map(Product::getId)
                .toList();
        productLikeRepository.findAllByProductId(productIds);

    }
}
