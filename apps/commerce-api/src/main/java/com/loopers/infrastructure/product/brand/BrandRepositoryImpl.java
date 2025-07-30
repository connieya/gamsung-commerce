package com.loopers.infrastructure.product.brand;

import com.loopers.domain.product.brand.Brand;
import com.loopers.domain.product.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@RequiredArgsConstructor
@Repository
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Brand save(Brand brand) {
        return brandJpaRepository.save(BrandEntity.fromDomain(brand)).toDomain();
    }

    @Override
    public Optional<Brand> findBrand(Long brandId) {
        return brandJpaRepository.findById(brandId)
                .map(BrandEntity::toDomain);
    }
}
