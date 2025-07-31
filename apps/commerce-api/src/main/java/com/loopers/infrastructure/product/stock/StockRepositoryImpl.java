package com.loopers.infrastructure.product.stock;

import com.loopers.domain.product.stock.Stock;
import com.loopers.domain.product.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final StockJpaRepository stockJpaRepository;

    @Override
    public Stock save(Stock stock) {
        return stockJpaRepository.save(StockEntity.from(stock)).toDomain();
    }
}
