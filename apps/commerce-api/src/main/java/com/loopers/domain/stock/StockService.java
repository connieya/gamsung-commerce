// [LLD-SVC-01] StockService — docs/lld/stock-reservation.md > 도메인 레이어 2-5
package com.loopers.domain.stock;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockReservationRepository reservationRepository;

    // [LLD-SVC-01] reserve() — docs/lld/stock-reservation.md > 도메인 레이어 2-5
    @Transactional
    public void reserve(StockCommand.ReserveStocks command) {
        List<Long> productIds = command.getItems().stream()
                .map(StockCommand.ReserveStocks.Item::getProductId).toList();
        Map<Long, Long> qtyMap = command.getItems().stream()
                .collect(Collectors.toMap(
                        StockCommand.ReserveStocks.Item::getProductId,
                        StockCommand.ReserveStocks.Item::getQuantity
                ));

        List<Stock> stocks = stockRepository.findStocksForUpdate(productIds);

        List<StockReservation> reservations = stocks.stream().map(stock -> {
            Long qty = qtyMap.get(stock.getProductId());
            stock.reserve(qty);
            return StockReservation.create(stock.getId(), command.getOrderId(), qty);
        }).toList();

        stockRepository.saveAll(stocks);
        reservationRepository.saveAll(reservations);
    }

    // [LLD-SVC-01] confirm() — docs/lld/stock-reservation.md > 도메인 레이어 2-5
    @Transactional
    public void confirm(StockCommand.ConfirmReservation command) {
        List<StockReservation> reservations =
                reservationRepository.findByOrderIdForUpdate(command.getOrderId());

        List<Long> stockIds = reservations.stream()
                .map(StockReservation::getStockId).toList();
        Map<Long, Stock> stockMap = stockRepository.findStocksForUpdateByIds(stockIds).stream()
                .collect(Collectors.toMap(Stock::getId, s -> s));

        reservations.forEach(reservation -> {
            Stock stock = Optional.ofNullable(stockMap.get(reservation.getStockId()))
                    .orElseThrow(() -> new CoreException(ErrorType.STOCK_NOT_FOUND));
            reservation.confirm();
            stock.confirmReservation(reservation.getQuantity());
        });

        stockRepository.saveAll(new ArrayList<>(stockMap.values()));
        reservationRepository.saveAll(reservations);
    }

    // [LLD-SVC-01] cancel() — docs/lld/stock-reservation.md > 도메인 레이어 2-5
    @Transactional
    public void cancel(StockCommand.CancelReservation command) {
        List<StockReservation> reservations =
                reservationRepository.findByOrderIdForUpdate(command.getOrderId());

        List<Long> stockIds = reservations.stream()
                .map(StockReservation::getStockId).toList();
        Map<Long, Stock> stockMap = stockRepository.findStocksForUpdateByIds(stockIds).stream()
                .collect(Collectors.toMap(Stock::getId, s -> s));

        reservations.forEach(reservation -> {
            Stock stock = Optional.ofNullable(stockMap.get(reservation.getStockId()))
                    .orElseThrow(() -> new CoreException(ErrorType.STOCK_NOT_FOUND));
            reservation.cancel();
            stock.releaseReservation(reservation.getQuantity());
        });

        stockRepository.saveAll(new ArrayList<>(stockMap.values()));
        reservationRepository.saveAll(reservations);
    }
}
