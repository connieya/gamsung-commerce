// [LLD-FACADE-01] StockFacade — docs/lld/stock-reservation.md > Application 레이어 4-1
package com.loopers.application.stock;

import com.loopers.domain.stock.StockCommand;
import com.loopers.domain.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockFacade {

    private final StockService stockService;

    // [LLD-FACADE-01] reserve() — docs/lld/stock-reservation.md > Application 레이어 4-1
    public void reserve(StockCommand.ReserveStocks command) {
        stockService.reserve(command);
    }

    // [LLD-FACADE-01] cancel() — docs/lld/stock-reservation.md > Application 레이어 4-1
    public void cancel(StockCommand.CancelReservation command) {
        stockService.cancel(command);
    }
}
