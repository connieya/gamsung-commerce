package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandInfo;
import com.loopers.domain.brand.BrandService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/brands")
public class BrandV1Controller implements BrandV1ApiSpec{

    private final BrandService brandService;

    @GetMapping("/{brandId}")
    @Override
    public ApiResponse<?> getBrand(@PathVariable(value = "brandId") Long brandId) {
        BrandInfo brandInfo = brandService.getBrandInfo(brandId);
        return ApiResponse.success(brandInfo);
    }
}
