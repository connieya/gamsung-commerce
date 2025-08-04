package com.loopers.infrastructure.product.stock;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface StockJpaRepository extends JpaRepository<StockEntity, Long> {
    List<StockEntity> findByProductIdIn(Collection<Long> productIds);
}
