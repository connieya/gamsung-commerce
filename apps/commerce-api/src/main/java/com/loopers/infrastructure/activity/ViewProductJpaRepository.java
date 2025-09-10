package com.loopers.infrastructure.activity;

import com.loopers.domain.activity.ViewProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ViewProductJpaRepository extends JpaRepository<ViewProduct, Long> {
    Optional<ViewProduct> findByProductId(Long productId);
}
