package com.loopers.interfaces.api.sku;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Sku V1 API", description = "SKU / 상품 옵션 조회 API 입니다.")
public interface SkuV1ApiSpec {

    @Operation(summary = "상품별 옵션 목록 조회", description = "상품 ID로 등록된 옵션 목록을 조회합니다.")
    ApiResponse<SkuV1Dto.Response.OptionList> getOptions(
            @PathVariable("productId") Long productId
    );

    @Operation(summary = "SKU 단건 조회", description = "SKU ID로 SKU 정보를 조회합니다.")
    ApiResponse<SkuV1Dto.Response.Sku> getSku(
            @PathVariable("skuId") Long skuId
    );

    @Operation(summary = "상품별 SKU 목록 조회", description = "상품 ID로 등록된 SKU 목록을 조회합니다.")
    ApiResponse<SkuV1Dto.Response.SkuList> getSkusByProduct(
            @PathVariable("productId") Long productId
    );
}
