package com.loopers.application.order;

import com.loopers.domain.user.UserService;
import com.loopers.domain.user.exception.UserException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @InjectMocks
    OrderFacade orderFacade;

    @Mock
    UserService userService;

    @Test
    @DisplayName("주문: 존재하지 않는 유저로 주문 시도 시, 유저를 찾을 수 없다는 예외가 발생하며 실패한다.")
    void placeOrder_fails_whenUserNotFound() {
        // given
        OrderCriteria.OrderItem orderItem = OrderCriteria.OrderItem
                .builder()
                .productId(1L)
                .quantity(10L)
                .build();
        OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem), 1L , UUID.randomUUID().toString());

        // when
        when(userService.findByUserId("gunny"))
                .thenThrow(new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));

        // then
        assertThatThrownBy(() -> {
            orderFacade.place(orderCriteria);
        }).isInstanceOf(UserException.UserNotFoundException.class);
    }


}
