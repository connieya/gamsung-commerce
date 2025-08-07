package com.loopers.application.order;

import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.fixture.ProductFixture;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

class OrderCriteriaTest {

    @Test
    @DisplayName("OrderCriteria의 toCommand 메서드가 상품 정보와 수량을 반영하여 주문 커맨드의 총액과 항목을 정확히 생성한다.")
    void toCommand() {
        // given
        OrderCriteria.OrderItem orderItem1 = OrderCriteria.OrderItem.builder()
                .productId(1L)
                .quantity(10L)
                .build();

        OrderCriteria.OrderItem orderItem2 = OrderCriteria.OrderItem.builder()
                .productId(2L)
                .quantity(5L)
                .build();
        OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem1, orderItem2));

        // when
        Product productA = ProductFixture.complete()
                .set(Select.field(Product::getId), 1L)
                .set(Select.field(Product::getName), "상품A")
                .set(Select.field(Product::getPrice), 5000L)
                .create();


        Product productB = ProductFixture.complete()
                .set(Select.field(Product::getId), 2L)
                .set(Select.field(Product::getName), "상품B")
                .set(Select.field(Product::getPrice), 2000L)
                .create();

        OrderCommand command = OrderCommandMapper.map(1L, orderCriteria, List.of(productA, productB) ,10000L);

        // then
        assertAll(
                () -> assertThat(command.getDiscountAmount()).isEqualTo(10000L),
                () -> assertThat(command.getUserId()).isEqualTo(1L),
                () -> assertThat(command.getOrderItems()).hasSize(2)
                        .extracting("productId", "quantity", "price")
                        .containsExactlyInAnyOrder(
                                tuple(1L, 10L, 5000L),
                                tuple(2L, 5L, 2000L)
                        )
        );
    }
}
