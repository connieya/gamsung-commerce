package com.loopers.infrastructure.product.stock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StockJpaRepository extends JpaRepository<StockEntity, Long> {
    List<StockEntity> findByProductIdIn(Collection<Long> productIds);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockEntity s WHERE s.productId IN :productIds ")
    List<StockEntity> findByProductIdInForUpdate(@Param("productIds") List<Long> productIds);
}
