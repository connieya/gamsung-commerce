package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class BrandTest {

    @DisplayName("브랜드 생성")
    @Nested
    class Create {

        @DisplayName("이름이 유효하지 않으면 CoreException 이 발생한다.")
        @ValueSource(strings = {
                "", " "
        })
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidName(String name) {
            // given
            String description = "Just do it";

            // when & then
            assertThatThrownBy(() -> {
                Brand.create(name, description);
            }).isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }


        @DisplayName("설명이 유효하지 않으면 CoreException 이 발생한다.")
        @ValueSource(strings = {
                "", " "
        })
        @NullSource
        @ParameterizedTest
        void throwException_withInvalidDescription(String description) {
            // given
            String name = "Nike";

            // when & then
            assertThatThrownBy(() -> {
                Brand.create(name, description);
            }).isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

    }

}
