package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StockJpaRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByProductIdIn(Collection<Long> productIds);

    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.productId IN :productIds ")
    List<Stock> findByProductIdInForUpdate(@Param("productIds") List<Long> productIds);
}
