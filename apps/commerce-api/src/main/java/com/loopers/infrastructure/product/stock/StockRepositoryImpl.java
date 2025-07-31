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
}
