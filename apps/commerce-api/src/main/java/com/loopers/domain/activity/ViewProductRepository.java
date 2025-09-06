package com.loopers.domain.activity;

import java.util.Optional;

public interface ViewProductRepository {
    
    void save(ViewProduct viewProduct);

    Optional<ViewProduct> findByProductId(Long productId);
}
