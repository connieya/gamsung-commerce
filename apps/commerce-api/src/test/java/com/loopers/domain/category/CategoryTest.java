package com.loopers.domain.category;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class CategoryTest {

    @DisplayName("최상위 카테고리 생성")
    @Nested
    class CreateRoot {

        @DisplayName("이름이 유효하지 않으면 CoreException이 발생한다.")
        @NullSource
        @ValueSource(strings = {"", " "})
        @ParameterizedTest
        void throwException_withInvalidName(String name) {
            // when & then
            assertThatThrownBy(() -> Category.createRoot(name, 1))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @DisplayName("최상위 카테고리를 생성한다.")
        @Test
        void createRootCategory() {
            // when
            Category category = Category.createRoot("상의", 1);

            // then
            assertAll(
                    () -> assertThat(category.getName()).isEqualTo("상의"),
                    () -> assertThat(category.getParentId()).isNull(),
                    () -> assertThat(category.getDepth()).isEqualTo(1),
                    () -> assertThat(category.getDisplayOrder()).isEqualTo(1),
                    () -> assertThat(category.isRoot()).isTrue()
            );
        }
    }

    @DisplayName("하위 카테고리 생성")
    @Nested
    class CreateChild {

        @DisplayName("이름이 유효하지 않으면 CoreException이 발생한다.")
        @NullSource
        @ValueSource(strings = {"", " "})
        @ParameterizedTest
        void throwException_withInvalidName(String name) {
            // given
            Category parent = Category.createRoot("상의", 1);

            // when & then
            assertThatThrownBy(() -> Category.createChild(name, parent, 1))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.BAD_REQUEST);
        }

        @DisplayName("하위 카테고리를 생성한다.")
        @Test
        void createChildCategory() {
            // given
            Category parent = Category.createRoot("상의", 1);

            // when
            Category child = Category.createChild("반소매 티셔츠", parent, 1);

            // then
            assertAll(
                    () -> assertThat(child.getName()).isEqualTo("반소매 티셔츠"),
                    () -> assertThat(child.getParentId()).isEqualTo(parent.getId()),
                    () -> assertThat(child.getDepth()).isEqualTo(2),
                    () -> assertThat(child.getDisplayOrder()).isEqualTo(1),
                    () -> assertThat(child.isRoot()).isFalse()
            );
        }

        @DisplayName("부모의 depth에 1을 더한 값으로 depth가 설정된다.")
        @Test
        void childDepthIsParentDepthPlusOne() {
            // given
            Category root = Category.createRoot("상의", 1);
            Category child = Category.createChild("반소매 티셔츠", root, 1);

            // when
            Category grandChild = Category.createChild("오버핏 반소매", child, 1);

            // then
            assertThat(grandChild.getDepth()).isEqualTo(3);
        }
    }
}
