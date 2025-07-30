package com.loopers.domain.product.brand;


import java.util.Optional;

public interface BrandRepository {
    Brand save(Brand brand);

    Optional<Brand> findBrand(Long brandId);

}
