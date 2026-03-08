package com.loopers.application.payment;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.Category;
import com.loopers.domain.coupon.*;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.exception.PointException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.stock.Stock;
import com.loopers.domain.stock.StockRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

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
    CouponRepository couponRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CategoryJpaRepository categoryJpaRepository;

    @MockitoBean
    OrderApiClient orderApiClient;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("결제 성공 시, 유저 포인트가 정상적으로 차감되어야 한다.")
    void pay_updatesStateCorrectly_onSuccessfulPayment() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Point initialPoint = Point.create(savedUser.getUserId(), 10_000L);
        pointRepository.save(initialPoint);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product1 = ProductFixture.create().price(1000L).brand(savedBrand).categoryId(category.getId()).build();
        Product product2 = ProductFixture.create().price(2000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct1 = productRepository.save(product1);
        Product savedProduct2 = productRepository.save(product2);

        Stock stock1 = Stock.create(savedProduct1.getId(), 5L);
        Stock stock2 = Stock.create(savedProduct2.getId(), 10L);
        stockRepository.save(stock1);
        stockRepository.save(stock2);

        // Mock: OrderApiClient 응답
        OrderApiDto.OrderResponse orderResponse = new OrderApiDto.OrderResponse(
                1L, "ORD-TEST-001", 5000L, 0L, 5000L, "INIT", savedUser.getId(),
                List.of(
                        new OrderApiDto.OrderLineResponse(savedProduct1.getId(), 1L, 1000L),
                        new OrderApiDto.OrderLineResponse(savedProduct2.getId(), 2L, 2000L)
                )
        );
        when(orderApiClient.getOrder(1L)).thenReturn(ApiResponse.success(orderResponse));

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(
                "gunny", 1L, PaymentMethod.POINT, PayKind.POINT, CardType.HYUNDAI, "1234-1234-1234-1234", 1L
        );

        // when
        paymentFacade.pay(criteria);

        // then
        Point updatedPoint = pointRepository.findByUserId("gunny").get();
        assertThat(updatedPoint.getValue()).isEqualTo(5000L);
    }

    @Test
    @DisplayName("주문 시 유저의 포인트 잔액이 부족할 경우 주문은 실패해야 한다")
    void pay_throwsException_whenPointInsufficientException() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Point point = Point.create(savedUser.getUserId(), 3000L);
        pointRepository.save(point);

        // Mock: OrderApiClient 응답 (finalAmount: 5000L > point: 3000L)
        OrderApiDto.OrderResponse orderResponse = new OrderApiDto.OrderResponse(
                1L, "ORD-TEST-002", 5000L, 0L, 5000L, "INIT", savedUser.getId(),
                List.of(new OrderApiDto.OrderLineResponse(1L, 1L, 5000L))
        );
        when(orderApiClient.getOrder(1L)).thenReturn(ApiResponse.success(orderResponse));

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(
                "gunny", 1L, PaymentMethod.POINT, PayKind.POINT, CardType.HYUNDAI, "1234-1234-1234-1234", 1L
        );

        // when & then
        assertThatThrownBy(() -> paymentFacade.pay(criteria))
                .isInstanceOf(PointException.PointInsufficientException.class);
    }
}
