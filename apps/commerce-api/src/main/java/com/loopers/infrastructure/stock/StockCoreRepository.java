// [LLD-INFRA-04] StockCoreRepository — docs/lld/stock-reservation.md > 인프라 레이어 3-3
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

    // [LLD-INFRA-04] findStocksForUpdateByIds — docs/lld/stock-reservation.md > 인프라 레이어 3-3
    @Override
    public List<Stock> findStocksForUpdateByIds(List<Long> ids) {
        return stockJpaRepository.findByIdInForUpdate(ids);
    }
}
