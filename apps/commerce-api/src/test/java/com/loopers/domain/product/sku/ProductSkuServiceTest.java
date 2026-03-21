package com.loopers.domain.product.sku;

import com.loopers.domain.product.option.OptionType;
import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.option.ProductOptionRepository;
import com.loopers.domain.product.sku.exception.SkuException;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ProductSkuServiceTest {

    @InjectMocks
    ProductSkuService productSkuService;

    @Mock
    ProductSkuRepository productSkuRepository;

    @Mock
    ProductOptionRepository productOptionRepository;

    @DisplayName("SKU 생성")
    @Nested
    class CreateSku {

        @DisplayName("유효한 옵션 조합으로 SKU를 생성한다.")
        @Test
        void createSku_success() {
            // given
            Long productId = 1L;
            ProductOption colorOption = ProductOption.create(productId, OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(productId, OptionType.SIZE, "M");

            ProductSkuCommand.Create command = new ProductSkuCommand.Create(
                    productId, "P001-RED-M", 0L, List.of(1L, 2L)
            );

            ProductSku savedSku = ProductSku.create(productId, "P001-RED-M", 0L);

            doReturn(List.of(colorOption, sizeOption)).when(productOptionRepository).findAllByIdIn(List.of(1L, 2L));
            doReturn(List.of()).when(productSkuRepository).findByProductId(productId);
            doReturn(savedSku).when(productSkuRepository).save(any(ProductSku.class));

            // when
            ProductSku result = productSkuService.createSku(command);

            // then
            assertThat(result.getProductId()).isEqualTo(productId);
            assertThat(result.getSkuCode()).isEqualTo("P001-RED-M");
        }

        @DisplayName("동일한 옵션 조합의 SKU가 이미 존재하면 예외를 던진다.")
        @Test
        void throwException_whenDuplicateOptionCombination() {
            // given
            Long productId = 1L;
            ProductOption colorOption = ProductOption.create(productId, OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(productId, OptionType.SIZE, "M");

            ProductSkuCommand.Create command = new ProductSkuCommand.Create(
                    productId, "P001-RED-M", 0L, List.of(1L, 2L)
            );

            // 기존 SKU가 동일한 옵션 조합을 가지고 있는 경우
            ProductSku existingSku = ProductSku.create(productId, "P001-RED-M-OLD", 0L);
            existingSku.addOption(ProductSkuOption.create(existingSku, 1L));
            existingSku.addOption(ProductSkuOption.create(existingSku, 2L));

            doReturn(List.of(colorOption, sizeOption)).when(productOptionRepository).findAllByIdIn(List.of(1L, 2L));
            doReturn(List.of(existingSku)).when(productSkuRepository).findByProductId(productId);

            // when & then
            assertThatThrownBy(() -> productSkuService.createSku(command))
                    .isInstanceOf(SkuException.DuplicateOptionCombinationException.class);
        }

        @DisplayName("다른 상품에 속한 옵션을 사용하면 예외를 던진다.")
        @Test
        void throwException_whenOptionBelongsToAnotherProduct() {
            // given
            Long productId = 1L;
            Long anotherProductId = 2L;

            // 다른 상품에 속한 옵션
            ProductOption colorOption = ProductOption.create(anotherProductId, OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(productId, OptionType.SIZE, "M");

            ProductSkuCommand.Create command = new ProductSkuCommand.Create(
                    productId, "P001-RED-M", 0L, List.of(1L, 2L)
            );

            doReturn(List.of(colorOption, sizeOption)).when(productOptionRepository).findAllByIdIn(List.of(1L, 2L));

            // when & then
            assertThatThrownBy(() -> productSkuService.createSku(command))
                    .isInstanceOf(CoreException.class)
                    .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        }

        @DisplayName("존재하지 않는 옵션 ID가 포함되어 있으면 예외를 던진다.")
        @Test
        void throwException_whenOptionNotFound() {
            // given
            Long productId = 1L;
            ProductSkuCommand.Create command = new ProductSkuCommand.Create(
                    productId, "P001-RED-M", 0L, List.of(1L, 2L)
            );

            // 옵션 하나만 반환 (하나는 존재하지 않음)
            ProductOption colorOption = ProductOption.create(productId, OptionType.COLOR, "빨강");
            doReturn(List.of(colorOption)).when(productOptionRepository).findAllByIdIn(List.of(1L, 2L));

            // when & then
            assertThatThrownBy(() -> productSkuService.createSku(command))
                    .isInstanceOf(CoreException.class)
                    .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.OPTION_NOT_FOUND));
        }
    }

    @DisplayName("SKU 단건 조회")
    @Nested
    class GetSku {

        @DisplayName("존재하지 않는 SKU ID로 조회 시 예외를 던진다.")
        @Test
        void throwException_whenSkuNotFound() {
            // given
            doReturn(Optional.empty()).when(productSkuRepository).findById(999L);

            // when & then
            assertThatThrownBy(() -> productSkuService.getSku(999L))
                    .isInstanceOf(SkuException.SkuNotFoundException.class);
        }

        @DisplayName("존재하는 SKU ID로 조회 시 SKU를 반환한다.")
        @Test
        void getSku_success() {
            // given
            Long skuId = 1L;
            ProductSku sku = ProductSku.create(1L, "P001-RED-M", 0L);
            doReturn(Optional.of(sku)).when(productSkuRepository).findById(skuId);

            // when
            ProductSku result = productSkuService.getSku(skuId);

            // then
            assertThat(result.getSkuCode()).isEqualTo("P001-RED-M");
        }
    }
}
