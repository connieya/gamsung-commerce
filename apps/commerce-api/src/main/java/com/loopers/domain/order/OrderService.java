package com.loopers.domain.order;

import com.loopers.domain.order.exception.OrderException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderInfo place(OrderCommand orderCommand) {
        Order order = Order.create(orderCommand);

        return OrderInfo.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderInfo getOrderDetail(Long orderId) {
        Order order = orderRepository.findOrderDetailById(orderId)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));

        return OrderInfo.from(order);
    }
}
