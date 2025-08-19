package com.loopers.application.payment;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.stock.StockCommand;
import com.loopers.domain.stock.StockService;
import com.loopers.domain.order.Order;
import com.loopers.infrastructure.payment.PgClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final PointService pointService;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final PgClient pgClient;

    @Transactional
    public PaymentResult pay(PaymentCriteria.Pay criteria) {
        Order order = orderService.getOrder(criteria.orderId());

        // 포인트 차감
        if (criteria.paymentMethod()== PaymentMethod.POINT) {
            pointService.deduct(criteria.userId(), order.getFinalAmount());
        } else if (criteria.paymentMethod() == PaymentMethod.CARD) {
                pgClient.request(criteria.userId());
        }


        // 재고 차감
        List<StockCommand.DeductStocks.Item> items = order.getOrderLines()
                .stream()
                .map(orderLine -> StockCommand.DeductStocks.Item.builder()
                        .productId(orderLine.getProductId())
                        .quantity(orderLine.getQuantity())
                        .build()).toList();
        StockCommand.DeductStocks deductStocks = StockCommand.DeductStocks.create(items);
        stockService.deduct(deductStocks);

        Payment pay = paymentService.pay(criteria.toCommand(order.getFinalAmount()));
        orderService.complete(order.getId());

        return PaymentResult.from(pay);
    }
}
