package com.loopers.domain.stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {

    Stock save(Stock stock);

    Optional<Stock> findById(Long id);

    List<Stock> findByProductIdIn(List<Long> productIds);

    List<Stock> saveAll(List<Stock> stocks);

    List<Stock> findStocksForUpdate(List<Long> productIds);
}
