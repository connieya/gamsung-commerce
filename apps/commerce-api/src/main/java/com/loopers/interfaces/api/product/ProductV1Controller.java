package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductService productService;

    @Override
    public ApiResponse<?> getProducts() {
        return null;
    }

    @Override
    public ApiResponse<?> getProduct(Long productId) {
        ProductDetailInfo product = productService.getProduct(productId);
        return null;
    }
}
