package com.loopers.domain.brand;

import com.loopers.domain.brand.exception.BrandException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandInfo getBrandInfo(Long brandId) {
        Brand brand = brandRepository.findBrand(brandId).orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
        return BrandInfo.from(brand);
    }
}
