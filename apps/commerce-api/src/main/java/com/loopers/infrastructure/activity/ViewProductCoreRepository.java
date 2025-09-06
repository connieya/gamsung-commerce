package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.ViewProduct;
import com.loopers.domain.activity.ViewProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ViewProductCoreRepository implements ViewProductRepository {

    private final ViewProductJpaRepository viewProductJpaRepository;

    @Override
    public void save(ViewProduct viewProduct) {
        viewProductJpaRepository.save(viewProduct);
    }

    @Override
    public Optional<ViewProduct> findByProductId(Long productId) {
        return viewProductJpaRepository.findByProductId(productId);
    }
}
