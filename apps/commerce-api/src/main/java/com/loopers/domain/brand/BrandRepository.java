package com.loopers.domain.brand;


import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    Brand save(Brand brand);

    Optional<Brand> findBrand(Long brandId);

    List<Brand> findAllById(List<Long> brandIds);
}
