package com.loopers.domain.brand;

import java.util.Optional;

public interface BrandCacheRepository {

    Optional<Brand> findById(Long brandId);

    void save(Brand brandFromDb);
}
