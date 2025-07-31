package com.loopers.domain.product.stock;

import com.loopers.domain.product.exception.ProductException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

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
