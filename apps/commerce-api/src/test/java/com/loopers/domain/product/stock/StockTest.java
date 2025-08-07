package com.loopers.domain.product.stock;

import com.loopers.domain.product.exception.ProductException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {



    @DisplayName("1개 미만 정수로 재고 생성 시 실패한다.")
    @ParameterizedTest
    @ValueSource(longs = {
            0L,
            -1L
    })
    void createFail(Long value){
        // when & then
        assertThatThrownBy(() -> Stock.create(1L, value))
                .isInstanceOf(ConstraintViolationException.class);
    }


    @Test
    @DisplayName("주문한 수량만큼 재고를 차감한다.")
    void deduct_Success() {
        // given
        Stock stock = Stock.create(1L, 100L);

        // when
        stock.deduct(5L);

        // then
        assertThat(stock.getQuantity()).isEqualTo(95L);
    }

    @Test
    @DisplayName("주문한 수량보다 상품 재고가 작으면 InsufficientStockException 예외가 발생한다.")
    void deduct_Fail() {
        // given
        Stock stock = Stock.create(1L, 10L);

        // when & then
        assertThatThrownBy(() -> stock.deduct(15L))
                .isInstanceOf(ProductException.InsufficientStockException.class);


    }
}
