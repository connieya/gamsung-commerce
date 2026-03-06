package com.loopers.interfaces.api.category;

import com.loopers.domain.product.ProductSort;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.ProductV1Dto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Category V1 API", description = "카테고리 관련 API 입니다.")
public interface CategoryV1ApiSpec {

    @Operation(
            summary = "카테고리 트리 조회",
            description = "전체 카테고리를 트리 구조로 조회합니다."
    )
    ApiResponse<CategoryV1Dto.Response.CategoryTree> getCategories();

    @Operation(
            summary = "카테고리별 상품 목록 조회",
            description = "특정 카테고리에 속한 상품 목록을 조회합니다."
    )
    ApiResponse<ProductV1Dto.Response.Summary> getProductsByCategoryId(Long categoryId, int page, int size, ProductSort productSort);
}
