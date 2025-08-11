package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @DisplayName("상품 생성")
    @Nested
    class Create {

        @DisplayName("이름이 유효하지 않으면 , CoreException 이 발생한다.")
        @NullSource
        @ValueSource(strings = {
                "", " "
        })
        @ParameterizedTest
        void throwException_withInvalidName(String name) {
            // given
            Long basePrice = 10000L;

            // when & then
            assertThatThrownBy(() -> {
                Product.create(name, basePrice ,1L , ZonedDateTime.now());
            }).isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);

        }

        @DisplayName("기본 가격이 유효하지 않으면, CoreException 발생한다.")
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -10000L, -500L, -1L,
        })
        @ParameterizedTest
        void throwException_withInvalidBasePrice(Long basePrice) {
            // given
            String name = "foo";

            // when & then
            assertThatThrownBy(() -> {
                Product.create(name, basePrice ,1L , ZonedDateTime.now());
            }).isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 등록 시 출시일이 없으면 , CoreException 이 발생한다.")
        @Test
        void throwException_withInvalidReleasedAt() {
            // given
            String name = "foo";
            Long basePrice = 10000L;

            // when & then
            assertThatThrownBy(() -> {
                Product.create(name, basePrice ,1L , null);
            }).isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @CsvSource(textBlock = """
                foo | 0 | 1 | 2025-08-06T10:00:00+09:00[Asia/Seoul]
                foo bar | 10000 | 2 | 2025-08-05T15:30:00+09:00[Asia/Seoul]
                foo bar X | 200000 | 3 | 2025-08-01T08:00:00+09:00[Asia/Seoul]
                """, delimiter = '|')
        @DisplayName("상품을 생성한다.")
        @ParameterizedTest
        void createNewProduct(String name , Long price , Long brandId , ZonedDateTime releasedAt) {
            // when
            Product product = Product.create(name, price, brandId, releasedAt);

            // then
            assertAll(
                    ()-> assertThat(product).isNotNull(),
                    ()-> assertThat(product.getName()).isEqualTo(name),
                    ()-> assertThat(product.getPrice()).isEqualTo(price)

            );
        }


    }
}
