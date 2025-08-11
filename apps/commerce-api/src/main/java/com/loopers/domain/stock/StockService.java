package com.loopers.domain.stock;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderLine;
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
    public void deduct(List<Long> productIds, OrderCommand orderCommand) {
        List<OrderCommand.OrderItem> orderItems = orderCommand.getOrderItems();
        Map<Long, Long> orderQuantities = orderItems.stream()
                .collect(Collectors.toMap(
                        OrderCommand.OrderItem::getProductId,
                        OrderCommand.OrderItem::getQuantity
                ));
        List<Stock> stocks = stockRepository.findByProductIdIn(productIds);

        stocks.forEach(stock -> {
            Long quantity = orderQuantities.get(stock.getProductId());
            stock.deduct(quantity);
        });

        stockRepository.saveAll(stocks);

    }

    @Transactional
    public void deduct(List<OrderLine> orderLines) {
        Map<Long, Long> orderQuantities = orderLines.stream()
                .collect(Collectors.toMap(
                        OrderLine::getProductId,
                        OrderLine::getQuantity
                ));

        List<Long> productIds = orderLines.stream()
                .mapToLong(OrderLine::getProductId)
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
