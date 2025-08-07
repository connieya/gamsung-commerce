package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.point.PointService;
import com.loopers.domain.point.exception.PointException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.product.stock.StockService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.exception.UserException;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.support.error.ErrorType;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @InjectMocks
    OrderFacade orderFacade;

    @Mock
    UserService userService;

    @Mock
    ProductService productService;

    @Mock
    OrderService orderService;

    @Mock
    PointService pointService;

    @Mock
    StockService stockService;


    @Test
    @DisplayName("주문: 존재하지 않는 유저로 주문 시도 시, 유저를 찾을 수 없다는 예외가 발생하며 실패한다.")
    void placeOrder_fails_whenUserNotFound() {
        // given
        OrderCriteria.OrderItem orderItem = OrderCriteria.OrderItem
                .builder()
                .productId(1L)
                .quantity(10L)
                .build();
        OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem));

        // when
        when(userService.findByUserId("gunny"))
                .thenThrow(new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        // then
        assertThatThrownBy(() -> {
            orderFacade.place(orderCriteria);
        }).isInstanceOf(UserException.UserNotFoundException.class);
    }


    @Test
    @DisplayName("주문: 보유 포인트가 부족하여 포인트 차감에 실패할 경우, 주문이 실패한다.")
    void placeOrder_fails_whenPointInsufficient() {
        // given
        OrderCriteria.OrderItem orderItem = OrderCriteria.OrderItem
                .builder()
                .productId(1L)
                .quantity(10L)
                .build();
        OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem));


        // when
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        when(userService.findByUserId("gunny"))
                .thenReturn(user);

        Product product = ProductFixture.complete()
                .set(Select.field(Product::getId), 1L)
                .set(Select.field(Product::getPrice), 2000L)
                .create();

        when(productService.findAllById(List.of(1L)))
                .thenReturn(List.of(product));


        when(orderService.place(any()))
                .thenReturn(OrderInfo.builder()
                        .orderItems(List.of())
                        .totalAmount(2000L)
                        .build());


        doThrow(new PointException.PointInsufficientException(ErrorType.POINT_INSUFFICIENT))
                .when(pointService).deduct("gunny", 2000L);

        //    then
        assertThatThrownBy(() -> {
            orderFacade.place(orderCriteria);
        }).isInstanceOf(PointException.PointInsufficientException.class);
    }


    @Test
    @DisplayName("주문: 상품 재고가 부족하여 재고 차감에 실패할 경우, 주문이 실패한다.")
    void placeOrder_fails_whenStockInsufficient() {
        // given
        OrderCriteria.OrderItem orderItem = OrderCriteria.OrderItem
                .builder()
                .productId(1L)
                .quantity(10L)
                .build();
        OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem));


        // when
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        when(userService.findByUserId("gunny"))
                .thenReturn(user);

        Product product = ProductFixture.complete()
                .set(Select.field(Product::getId), 1L)
                .set(Select.field(Product::getPrice), 2000L)
                .create();

        when(productService.findAllById(List.of(1L)))
                .thenReturn(List.of(product));


        when(orderService.place(any()))
                .thenReturn(OrderInfo.builder()
                        .orderItems(List.of())
                        .totalAmount(2000L)
                        .build());

        doNothing().when(pointService).deduct("gunny", 2000L);

        doThrow(new ProductException.InsufficientStockException(ErrorType.STOCK_INSUFFICIENT))
                .when(stockService).deduct(eq(List.of(1L)), any(OrderCommand.class));
        //    then
        assertThatThrownBy(() -> {
            orderFacade.place(orderCriteria);
        }).isInstanceOf(ProductException.InsufficientStockException.class);
    }

}
