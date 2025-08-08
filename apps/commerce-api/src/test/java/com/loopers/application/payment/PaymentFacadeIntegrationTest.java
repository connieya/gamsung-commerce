package com.loopers.application.payment;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderLine;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.exception.PointException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.product.stock.Stock;
import com.loopers.domain.product.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentFacadeIntegrationTest {

    @Autowired
    PaymentFacade paymentFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("재고가 존재하지 않거나 부족할 경우 주문은 실패해야 한다.")
    void pay_throwsException_whenStockIsInsufficient() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Point point = Point.create(savedUser.getUserId(), 1000_000L);
        pointRepository.save(point);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getPrice), 1000L).create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getPrice), 2000L).create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        Stock stock1 = Stock.create(savedProduct1.getId(), 5L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 150L);

        stockRepository.save(stock1);
        stockRepository.save(stock2);

        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                .productId(savedProduct1.getId())
                .price(1000L)
                .quantity(2L)
                .build();

        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                .productId(savedProduct2.getId())
                .price(2000L)
                .quantity(200L)
                .build();
        OrderCommand orderCommand = OrderCommand.of(savedUser.getId(), List.of(orderItem1, orderItem2), 5000L);
        Order order = Order.create(orderCommand);
        Order savedOrder = orderRepository.save(order);

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", savedOrder.getId(), PaymentMethod.POINT);

        // when &  then
        assertThatThrownBy(
                () -> {
                    paymentFacade.pay(criteria);
                }
        ).isInstanceOf(ProductException.InsufficientStockException.class);

    }

    @Test
    @DisplayName("주문 시 유저의 포인트 잔액이 부족할 경우 주문은 실패해야 한다")
    void pay_throwsException_whenPointInsufficientException() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Point point = Point.create(savedUser.getUserId(), 5000L);
        pointRepository.save(point);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getPrice), 1000L).create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getPrice), 2000L).create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        Stock stock1 = Stock.create(savedProduct1.getId(), 5L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 150L);

        stockRepository.save(stock1);
        stockRepository.save(stock2);

        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                .productId(savedProduct1.getId())
                .price(1000L)
                .quantity(2L)
                .build();

        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                .productId(savedProduct2.getId())
                .price(2000L)
                .quantity(10L)
                .build();
        OrderCommand orderCommand = OrderCommand.of(savedUser.getId(), List.of(orderItem1, orderItem2), 5000L);
        Order order = Order.create(orderCommand);
        Order savedOrder = orderRepository.save(order);

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay("gunny", savedOrder.getId(), PaymentMethod.POINT);

        // when &  then
        assertThatThrownBy(
                () -> {
                    paymentFacade.pay(criteria);
                }
        ).isInstanceOf(PointException.PointInsufficientException.class);

    }

}
