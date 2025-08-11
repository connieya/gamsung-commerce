package com.loopers.domain.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;



    @Transactional
    public void deduct(StockCommand.DeductStocks deductStocks) {
        List<StockCommand.DeductStocks.Item> items = deductStocks.getItems();

        Map<Long, Long> orderQuantities = items.stream()
                .collect(Collectors.toMap(
                        StockCommand.DeductStocks.Item::getProductId,
                        StockCommand.DeductStocks.Item::getQuantity
                ));

        List<Long> productIds = items.stream()
                .mapToLong(StockCommand.DeductStocks.Item::getProductId)
                .boxed()
                .toList();

        List<Stock> stocks = stockRepository.findStocksForUpdate(productIds);


        stocks.forEach(stock -> {
            Long quantity = orderQuantities.get(stock.getProductId());
            stock.deduct(quantity);
        });

        stockRepository.saveAll(stocks);
    }
}
