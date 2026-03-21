package com.loopers.interfaces.api.sku;

import com.loopers.application.sku.SkuFacade;
import com.loopers.application.sku.SkuResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SkuV1Controller implements SkuV1ApiSpec {

    private final SkuFacade skuFacade;

    @GetMapping("/api/v1/products/{productId}/options")
    @Override
    public ApiResponse<SkuV1Dto.Response.OptionList> getOptions(@PathVariable Long productId) {
        SkuResult.OptionList result = skuFacade.getOptions(productId);
        return ApiResponse.success(SkuV1Dto.Response.OptionList.from(result));
    }

    @GetMapping("/api/v1/skus/{skuId}")
    @Override
    public ApiResponse<SkuV1Dto.Response.Sku> getSku(@PathVariable Long skuId) {
        SkuResult.Sku result = skuFacade.getSku(skuId);
        return ApiResponse.success(SkuV1Dto.Response.Sku.from(result));
    }

    @GetMapping("/api/v1/products/{productId}/skus")
    @Override
    public ApiResponse<SkuV1Dto.Response.SkuList> getSkusByProduct(@PathVariable Long productId) {
        SkuResult.SkuList result = skuFacade.getSkusByProduct(productId);
        return ApiResponse.success(SkuV1Dto.Response.SkuList.from(result));
    }
}
