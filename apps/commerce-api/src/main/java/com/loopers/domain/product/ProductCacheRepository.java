package com.loopers.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCacheRepository {

    Page<ProductInfo> findProductDetails(Pageable pageable , Long brandId);

    void save(Long brandId, Page<ProductInfo> productInfoPage);
}
