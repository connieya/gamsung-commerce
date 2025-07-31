package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.stock.StockService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final PointService pointService;
    private final StockService stockService;

    @Transactional
    public void place(OrderCriteria orderCriteria) {
        User user = userService.findByUserId(orderCriteria.getUserId());
        List<Product> products = productService.findAllById(orderCriteria.getProductIds());
        OrderCommand command = orderCriteria.toCommand(products, user.getId());
        Order order = orderService.place(command);
        pointService.deduct(user.getUserId(), order.getTotalAmount());
        stockService.deduct(orderCriteria.getProductIds(), command);
    }
}
