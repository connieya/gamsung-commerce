// [LLD-REPO-02] StockRepository — docs/lld/stock-reservation.md > 도메인 레이어 2-6
package com.loopers.domain.stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {

    Stock save(Stock stock);

    Optional<Stock> findById(Long id);

    List<Stock> findByProductIdIn(List<Long> productIds);

    List<Stock> saveAll(List<Stock> stocks);

    List<Stock> findStocksForUpdate(List<Long> productIds);

    // [LLD-REPO-02] findStocksForUpdateByIds — docs/lld/stock-reservation.md > 도메인 레이어 2-6
    List<Stock> findStocksForUpdateByIds(List<Long> ids);
}
