package com.loopers.domain.order;

import com.loopers.domain.order.exception.OrderException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderInfo place(OrderCommand orderCommand) {
        Order order = Order.create(orderCommand);

        return OrderInfo.from(orderRepository.save(order));
    }

    @Transactional
    public OrderInfo place(OrderCommand orderCommand, String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return place(orderCommand);
        }
        Order order = Order.create(orderCommand, orderNo);
        return OrderInfo.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderInfo getOrderDetail(Long orderId) {
        Order order = orderRepository.findOrderDetailById(orderId)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));

        return OrderInfo.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderInfo> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(OrderInfo::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));
    }
    
    @Transactional(readOnly = true)
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Optional<Order> findOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional
    public void complete(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException.OrderNotFoundException(ErrorType.ORDER_NOT_FOUND));

        order.paid();
        orderRepository.save(order);
    }
}
