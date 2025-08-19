package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@RequiredArgsConstructor
@Repository
public class BrandCoreRepository implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Brand save(Brand brand) {
        return brandJpaRepository.save(brand);
    }

    @Override
    public Optional<Brand> findBrand(Long brandId) {
        return brandJpaRepository.findById(brandId);
    }

    @Override
    public List<Brand> findAllById(List<Long> brandIds) {
        return StreamSupport.stream(brandJpaRepository.findAllById(brandIds).spliterator(), false)
                .collect(Collectors.toList());
    }
}
