package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import org.springframework.data.repository.CrudRepository;

public interface BrandJpaRepository extends CrudRepository<Brand, Long> {

}
