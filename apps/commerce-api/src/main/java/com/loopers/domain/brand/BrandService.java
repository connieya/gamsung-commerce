package com.loopers.domain.brand;

import com.loopers.domain.brand.exception.BrandException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final BrandCacheRepository brandCacheRepository;

    public BrandInfo getBrandInfo(Long brandId) {
        Brand brand = brandCacheRepository.findById(brandId)
                .orElseGet(() -> {
                    Brand brandFromDb = brandRepository.findBrand(brandId)
                            .orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));
                    brandCacheRepository.save(brandFromDb);
                    return brandFromDb;
                });
        return BrandInfo.from(brand);
    }

    public List<BrandInfo> findAllById(List<Long> brandIds) {
        List<Brand> brands = brandRepository.findAllById(brandIds);
        return brands.stream().map(BrandInfo::from).toList();
    }

    @Transactional
    public void update(BrandCommand.UpdateBrand updateBrand) {
        Brand brand = brandRepository.findBrand(updateBrand.getBrandId())
                .orElseThrow(() -> new BrandException.BrandNotFoundException(ErrorType.BRAND_NOT_FOUND));

        brand.update(updateBrand.getName(), updateBrand.getDescription());
        // 캐시와 DB에 업데이트된 브랜드 정보를 저장
        brandCacheRepository.save(brand);

    }
}
