package com.loopers.infrastructure.product.stock;

import com.loopers.domain.product.stock.Stock;
import com.loopers.domain.product.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class StockRepositoryImpl implements StockRepository {

    private final StockJpaRepository stockJpaRepository;

    @Override
    public Stock save(Stock stock) {
        return stockJpaRepository.save(StockEntity.from(stock)).toDomain();
    }

    @Override
    public Optional<Stock> findById(Long id) {
        return stockJpaRepository.findById(id).map(StockEntity::toDomain);
    }

    @Override
    public List<Stock> findByProductIdIn(List<Long> productIds) {
        return stockJpaRepository.findByProductIdIn(productIds)
                .stream()
                .map(StockEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Stock> findStocksForUpdate(List<Long> productIds) {
        return stockJpaRepository.findByProductIdInForUpdate(productIds)
                .stream()
                .map(StockEntity::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Stock> saveAll(List<Stock> stocks) {
        List<StockEntity> stockEntities = stocks.stream()
                .map(stock -> {
                    // 도메인 객체의 ID를 기반으로 기존 엔티티를 찾아서 병합(merge)
                    // 또는 단순히 fromDomain()으로 변환
                    StockEntity entity = stockJpaRepository.findById(stock.getId())
                            .orElseGet(() -> StockEntity.from(stock));
                    entity.setQuantity(stock.getQuantity()); // 변경된 필드만 업데이트
                    return entity;
                })
                .collect(Collectors.toList());

        return stockJpaRepository.saveAll(stockEntities).stream()
                .map(StockEntity::toDomain)
                .collect(Collectors.toList());
    }
}
