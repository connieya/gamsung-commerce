package com.loopers.application.sku;

import com.loopers.domain.product.option.OptionType;
import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.option.ProductOptionService;
import com.loopers.domain.product.sku.ProductSku;
import com.loopers.domain.product.sku.ProductSkuOption;
import com.loopers.domain.product.sku.ProductSkuService;
import com.loopers.domain.product.sku.exception.SkuException;
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
    ProductOptionService productOptionService;

    @Mock
    ProductSkuService productSkuService;

    @DisplayName("SKU 단건 조회")
    @Nested
    class GetSku {

        @DisplayName("SKU를 정상적으로 조회한다.")
        @Test
        void getSku_success() {
            // given
            Long skuId = 1L;
            ProductSku sku = ProductSku.create(1L, "P001-RED-M", 0L);
            sku.addOption(ProductSkuOption.create(sku, 10L));

            ProductOption option = ProductOption.create(1L, OptionType.COLOR, "빨강");

            doReturn(sku).when(productSkuService).getSku(skuId);
            doReturn(List.of(option)).when(productOptionService).findAllByIdIn(List.of(10L));

            // when
            SkuResult.Sku result = skuFacade.getSku(skuId);

            // then
            assertAll(
                    () -> assertThat(result.skuCode()).isEqualTo("P001-RED-M"),
                    () -> assertThat(result.options()).hasSize(1)
            );
        }

        @DisplayName("존재하지 않는 SKU 조회 시 예외를 그대로 전파한다.")
        @Test
        void throwException_whenSkuNotFound() {
            // given
            doThrow(new SkuException.SkuNotFoundException(com.loopers.support.error.ErrorType.SKU_NOT_FOUND))
                    .when(productSkuService).getSku(999L);

            // when & then
            assertThatThrownBy(() -> skuFacade.getSku(999L))
                    .isInstanceOf(SkuException.SkuNotFoundException.class);
        }
    }

    @DisplayName("상품별 SKU 목록 조회")
    @Nested
    class GetSkusByProduct {

        @DisplayName("상품의 SKU 목록을 조회한다.")
        @Test
        void getSkusByProduct_success() {
            // given
            Long productId = 1L;
            ProductSku sku1 = ProductSku.create(productId, "P001-RED-M", 0L);
            ProductSku sku2 = ProductSku.create(productId, "P001-BLUE-L", 1000L);

            doReturn(List.of(sku1, sku2)).when(productSkuService).getSkusByProductId(productId);
            doReturn(List.of()).when(productOptionService).findAllByIdIn(any());

            // when
            SkuResult.SkuList result = skuFacade.getSkusByProduct(productId);

            // then
            assertThat(result.skus()).hasSize(2);
        }
    }
}
