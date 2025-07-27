package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Product V1 API", description = "상품 관련 API 입니다.")
public interface ProductV1ApiSpec {

    @Operation(
            summary = "상품 목록 조회",
            description = "상품 목록을 조회합니다."
    )
    ApiResponse<?> getProducts();


    @Operation(
            summary = "상품 정보 조회",
            description = "상품 ID로 상품 정보를 조회합니다."
    )
    ApiResponse<?> getProduct(
            @PathVariable("productId") Long productId
    );

}
