package com.loopers.application.order;

import com.loopers.domain.order.OrderInfo;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.exception.UserException;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.support.error.ErrorType;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    CouponService couponService;

    @Nested
    @DisplayName("주문 등록(place)")
    class place {
        @Test
        @DisplayName("주문: 존재하지 않는 유저로 주문 시도 시, 유저를 찾을 수 없다는 예외가 발생하며 실패한다.")
        void fails_whenUserNotFound() {
            // given
            OrderCriteria.OrderItem orderItem = OrderCriteria.OrderItem
                    .builder()
                    .productId(1L)
                    .quantity(10L)
                    .build();
            OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem), 1L);

            when(userService.findByUserId("gunny"))
                    .thenThrow(new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> orderFacade.place(orderCriteria))
                    .isInstanceOf(UserException.UserNotFoundException.class);
        }

        @Test
        @DisplayName("주문: 유저·상품·쿠폰이 정상이면 주문이 성공한다.")
        void succeeds() {
            // given
            OrderCriteria.OrderItem orderItem = OrderCriteria.OrderItem
                    .builder()
                    .productId(1L)
                    .quantity(10L)
                    .build();
            OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem), 1L);

            User user = UserFixture.complete().set(Select.field(User::getId), 1L).create();
            Product product = ProductFixture.complete()
                    .set(Select.field(Product::getId), 1L)
                    .set(Select.field(Product::getPrice), 10_000L)
                    .create();
            List<Product> products = List.of(product);

            long totalAmount = 100_000L;
            long discountAmount = 0L;

            when(userService.findByUserId("gunny")).thenReturn(user);
            when(productService.findAllById(List.of(1L))).thenReturn(products);
            when(couponService.calculateDiscountAmount(eq(1L), eq(totalAmount))).thenReturn(discountAmount);

            OrderInfo orderInfo = mock(OrderInfo.class);
            when(orderInfo.getOrderId()).thenReturn(1L);
            when(orderInfo.getTotalAmount()).thenReturn(totalAmount);
            when(orderInfo.getDiscountAmount()).thenReturn(discountAmount);
            when(orderService.place(any())).thenReturn(orderInfo);

            // when
            OrderResult.Create result = orderFacade.place(orderCriteria);

            // then
            assertThat(result.getOrderId()).isEqualTo(1L);
            assertThat(result.getTotalAmount()).isEqualTo(100_000L);
            assertThat(result.getDiscountAmount()).isEqualTo(0L);
        }
    }
}
