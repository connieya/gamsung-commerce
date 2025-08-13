package com.loopers.interfaces.api.product;

import com.loopers.domain.product.ProductSort;
import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductsInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductService productService;

    @GetMapping
    @Override
    public ApiResponse<ProductV1Dto.SummaryResponse> getProducts(
            @RequestParam int page
            , @RequestParam int size
            , @RequestParam ProductSort productSort) {
        ProductsInfo products = productService.getProducts(size, page, productSort);
        return ApiResponse.success(ProductV1Dto.SummaryResponse.from(products));
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.DetailResponse> getProduct(@PathVariable Long productId) {
        ProductDetailInfo productDetailInfo = productService.getProduct(productId);
        return ApiResponse.success(ProductV1Dto.DetailResponse.from(productDetailInfo));
    }
}
