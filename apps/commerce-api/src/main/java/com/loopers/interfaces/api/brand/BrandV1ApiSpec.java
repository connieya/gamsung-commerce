package com.loopers.interfaces.api.brand;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Brand V1 API", description = "브랜드 관련 API 입니다.")
public interface BrandV1ApiSpec {

    @Operation(
            summary = "브랜드 정보 조회",
            description = "브랜드 ID로 브랜드 정보를 조회하빈다."
    )
    ApiResponse<?> getBrand(
            @PathVariable("brandId") Long brandId
    );

}
