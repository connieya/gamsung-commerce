package com.loopers.application.sku;

import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.product.option.OptionType;
import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.option.ProductOptionService;
import com.loopers.domain.product.sku.ProductSku;
import com.loopers.domain.product.sku.ProductSkuOption;
import com.loopers.domain.product.sku.ProductSkuService;
import com.loopers.domain.product.sku.exception.SkuException;
import com.loopers.support.error.ErrorType;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class SkuFacadeTest {

    @InjectMocks
    SkuFacade skuFacade;

    @Mock
    ProductService productService;

    @Mock
    ProductOptionService productOptionService;

    @Mock
    ProductSkuService productSkuService;

    @DisplayName("상품 옵션 등록")
    @Nested
    class RegisterOption {

        @DisplayName("옵션을 정상적으로 등록한다.")
        @Test
        void registerOption_success() {
            // given
            Long productId = 1L;
            SkuCriteria.RegisterOption criteria = new SkuCriteria.RegisterOption(productId, OptionType.COLOR, "빨강");

            ProductOption savedOption = ProductOption.create(productId, OptionType.COLOR, "빨강");
            doReturn(savedOption).when(productOptionService).register(any());

            // when
            SkuResult.Option result = skuFacade.registerOption(criteria);

            // then
            assertAll(
                    () -> assertThat(result.productId()).isEqualTo(productId),
                    () -> assertThat(result.optionType()).isEqualTo(OptionType.COLOR),
                    () -> assertThat(result.optionValue()).isEqualTo("빨강")
            );
        }

        @DisplayName("존재하지 않는 상품에 옵션 등록 시 예외를 던진다.")
        @Test
        void throwException_whenProductNotFound() {
            // given
            SkuCriteria.RegisterOption criteria = new SkuCriteria.RegisterOption(999L, OptionType.COLOR, "빨강");
            doThrow(new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND))
                    .when(productService).assertExists(999L);

            // when & then
            assertThatThrownBy(() -> skuFacade.registerOption(criteria))
                    .isInstanceOf(ProductException.ProductNotFoundException.class);
        }
    }

    @DisplayName("SKU 등록")
    @Nested
    class RegisterSku {

        @DisplayName("SKU를 정상적으로 등록한다.")
        @Test
        void registerSku_success() {
            // given
            Long productId = 1L;
            SkuCriteria.RegisterSku criteria = new SkuCriteria.RegisterSku(productId, "P001-RED-M", 0L, List.of(1L, 2L));

            ProductOption colorOption = ProductOption.create(productId, OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(productId, OptionType.SIZE, "M");

            ProductSku sku = ProductSku.create(productId, "P001-RED-M", 0L);
            sku.addOption(ProductSkuOption.create(sku, 1L));
            sku.addOption(ProductSkuOption.create(sku, 2L));

            doReturn(List.of(colorOption, sizeOption)).when(productOptionService).findAllByIdIn(List.of(1L, 2L));
            doReturn(sku).when(productSkuService).createSku(any());

            // when
            SkuResult.Sku result = skuFacade.registerSku(criteria);

            // then
            assertAll(
                    () -> assertThat(result.productId()).isEqualTo(productId),
                    () -> assertThat(result.skuCode()).isEqualTo("P001-RED-M")
            );
        }

        @DisplayName("중복 옵션 조합 등록 시 예외를 그대로 전파한다.")
        @Test
        void throwException_whenDuplicateOptionCombination() {
            // given
            Long productId = 1L;
            SkuCriteria.RegisterSku criteria = new SkuCriteria.RegisterSku(productId, "P001-RED-M", 0L, List.of(1L, 2L));

            ProductOption colorOption = ProductOption.create(productId, OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(productId, OptionType.SIZE, "M");

            doReturn(List.of(colorOption, sizeOption)).when(productOptionService).findAllByIdIn(List.of(1L, 2L));
            doThrow(new SkuException.DuplicateOptionCombinationException(ErrorType.DUPLICATE_SKU_OPTION_COMBINATION))
                    .when(productSkuService).createSku(any());

            // when & then
            assertThatThrownBy(() -> skuFacade.registerSku(criteria))
                    .isInstanceOf(SkuException.DuplicateOptionCombinationException.class);
        }

        @DisplayName("존재하지 않는 옵션 ID 포함 시 예외를 던진다.")
        @Test
        void throwException_whenOptionNotFound() {
            // given
            Long productId = 1L;
            SkuCriteria.RegisterSku criteria = new SkuCriteria.RegisterSku(productId, "P001-RED-M", 0L, List.of(1L, 2L));

            // 옵션 하나만 반환 (하나는 존재하지 않음)
            ProductOption colorOption = ProductOption.create(productId, OptionType.COLOR, "빨강");
            doReturn(List.of(colorOption)).when(productOptionService).findAllByIdIn(List.of(1L, 2L));

            // when & then
            assertThatThrownBy(() -> skuFacade.registerSku(criteria))
                    .isInstanceOf(com.loopers.support.error.CoreException.class)
                    .satisfies(ex -> assertThat(((com.loopers.support.error.CoreException) ex).getErrorType())
                            .isEqualTo(ErrorType.OPTION_NOT_FOUND));
        }
    }
}
