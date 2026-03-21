package com.loopers.interfaces.api.sku;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Sku V1 API", description = "SKU / 상품 옵션 등록 API 입니다.")
public interface SkuV1ApiSpec {

    @Operation(summary = "상품 옵션 등록", description = "상품에 옵션(사이즈, 컬러 등)을 등록합니다.")
    ApiResponse<SkuV1Dto.Response.Option> registerOption(
            @PathVariable("productId") Long productId,
            @RequestBody SkuV1Dto.Request.RegisterOption request
    );

    @Operation(summary = "SKU 등록", description = "옵션 조합으로 SKU를 등록합니다.")
    ApiResponse<SkuV1Dto.Response.Sku> registerSku(
            @RequestBody SkuV1Dto.Request.RegisterSku request
    );
}
