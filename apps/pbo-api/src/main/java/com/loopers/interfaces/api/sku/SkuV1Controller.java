package com.loopers.interfaces.api.sku;

import com.loopers.application.sku.SkuCriteria;
import com.loopers.application.sku.SkuFacade;
import com.loopers.application.sku.SkuResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SkuV1Controller implements SkuV1ApiSpec {

    private final SkuFacade skuFacade;

    @PostMapping("/api/v1/products/{productId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<SkuV1Dto.Response.Option> registerOption(
            @PathVariable Long productId,
            @RequestBody SkuV1Dto.Request.RegisterOption request
    ) {
        SkuResult.Option result = skuFacade.registerOption(
                new SkuCriteria.RegisterOption(productId, request.optionType(), request.optionValue())
        );
        return ApiResponse.success(SkuV1Dto.Response.Option.from(result));
    }

    @PostMapping("/api/v1/skus")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public ApiResponse<SkuV1Dto.Response.Sku> registerSku(@RequestBody SkuV1Dto.Request.RegisterSku request) {
        SkuResult.Sku result = skuFacade.registerSku(
                new SkuCriteria.RegisterSku(
                        request.productId(),
                        request.skuCode(),
                        request.additionalPrice(),
                        request.optionIds()
                )
        );
        return ApiResponse.success(SkuV1Dto.Response.Sku.from(result));
    }
}
