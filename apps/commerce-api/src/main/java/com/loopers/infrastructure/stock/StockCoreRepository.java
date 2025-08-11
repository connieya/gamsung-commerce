package com.loopers.infrastructure.stock;

import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StockCoreRepository implements StockRepository {

    private final StockJpaRepository stockJpaRepository;

    @Override
    public Stock save(Stock stock) {
        return stockJpaRepository.save(stock);
    }

    @Override
    public Optional<Stock> findById(Long id) {
        return stockJpaRepository.findById(id);
    }

    @Override
    public List<Stock> findByProductIdIn(List<Long> productIds) {
        return stockJpaRepository.findByProductIdIn(productIds);
    }

    @Override
    public List<Stock> findStocksForUpdate(List<Long> productIds) {
        return stockJpaRepository.findByProductIdInForUpdate(productIds);
    }

    @Override
    public List<Stock> saveAll(List<Stock> stocks) {
        return stockJpaRepository.saveAll(stocks);
    }
}
