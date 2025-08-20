package com.loopers.application.payment;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.payment.processor.PaymentProcessor;
import com.loopers.domain.stock.StockCommand;
import com.loopers.domain.stock.StockService;
import com.loopers.domain.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderService orderService;
    private final StockService stockService;
    private final Map<String, PaymentProcessor> paymentProcessorMap;

    @Transactional
    public PaymentResult pay(PaymentCriteria.Pay criteria) {
        Order order = orderService.getOrder(criteria.orderId());
        order.validatePay();

        PaymentProcessor paymentProcessor = paymentProcessorMap.get(criteria.paymentMethod().toString());
        Payment payment = paymentProcessor.pay(criteria.toCommand(order.getFinalAmount()));

        // 재고 차감
        List<StockCommand.DeductStocks.Item> items = order.getOrderLines()
                .stream()
                .map(orderLine -> StockCommand.DeductStocks.Item.builder()
                        .productId(orderLine.getProductId())
                        .quantity(orderLine.getQuantity())
                        .build()).toList();
        StockCommand.DeductStocks deductStocks = StockCommand.DeductStocks.create(items);
        stockService.deduct(deductStocks);


        orderService.complete(order.getId());

        return PaymentResult.from(payment);
    }
}
