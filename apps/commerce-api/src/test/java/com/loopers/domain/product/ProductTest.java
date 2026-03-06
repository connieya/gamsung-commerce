package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProductTest {

    private final Brand mockBrand = mock(Brand.class);
    private final Long mockCategoryId = 1L;

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
                Product.create(name, basePrice, mockBrand, mockCategoryId, null, ZonedDateTime.now());
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
                Product.create(name, basePrice, mockBrand, mockCategoryId, null, ZonedDateTime.now());
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
                Product.create(name, basePrice, mockBrand, mockCategoryId, null, null);
            }).isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품을 생성한다.")
        @Test
        void createNewProduct() {
            // given
            String name = "foo";
            Long price = 10000L;
            ZonedDateTime releasedAt = ZonedDateTime.parse("2025-08-06T10:00:00+09:00[Asia/Seoul]");

            // when
            Product product = Product.create(name, price, mockBrand, mockCategoryId, null, releasedAt);

            // then
            assertAll(
                    ()-> assertThat(product).isNotNull(),
                    ()-> assertThat(product.getName()).isEqualTo(name),
                    ()-> assertThat(product.getPrice()).isEqualTo(price)

            );
        }


    }
}
