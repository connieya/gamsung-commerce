package com.loopers.domain.product.sku;

import com.loopers.domain.product.option.OptionType;
import com.loopers.domain.product.option.ProductOption;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ProductSkuServiceTest {

    @InjectMocks
    ProductSkuService productSkuService;

    @Mock
    ProductSkuRepository productSkuRepository;

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
                    productId, "P001-RED-M", 0L, List.of(1L, 2L), List.of(colorOption, sizeOption)
            );

            ProductSku savedSku = ProductSku.create(productId, "P001-RED-M", 0L);

            doReturn(false).when(productSkuRepository).existsBySkuCode("P001-RED-M");
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
                    productId, "P001-RED-M", 0L, List.of(1L, 2L), List.of(colorOption, sizeOption)
            );

            ProductSku existingSku = ProductSku.create(productId, "P001-RED-M-OLD", 0L);
            existingSku.addOption(ProductSkuOption.create(existingSku, 1L));
            existingSku.addOption(ProductSkuOption.create(existingSku, 2L));

            doReturn(false).when(productSkuRepository).existsBySkuCode("P001-RED-M");
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

            ProductOption colorOption = ProductOption.create(anotherProductId, OptionType.COLOR, "빨강");
            ProductOption sizeOption = ProductOption.create(productId, OptionType.SIZE, "M");

            ProductSkuCommand.Create command = new ProductSkuCommand.Create(
                    productId, "P001-RED-M", 0L, List.of(1L, 2L), List.of(colorOption, sizeOption)
            );

            // when & then
            assertThatThrownBy(() -> productSkuService.createSku(command))
                    .isInstanceOf(CoreException.class)
                    .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        }

        @DisplayName("이미 사용 중인 SKU 코드이면 예외를 던진다.")
        @Test
        void throwException_whenSkuCodeAlreadyExists() {
            // given
            Long productId = 1L;
            ProductOption colorOption = ProductOption.create(productId, OptionType.COLOR, "빨강");

            ProductSkuCommand.Create command = new ProductSkuCommand.Create(
                    productId, "P001-RED", 0L, List.of(1L), List.of(colorOption)
            );

            doReturn(true).when(productSkuRepository).existsBySkuCode("P001-RED");

            // when & then
            assertThatThrownBy(() -> productSkuService.createSku(command))
                    .isInstanceOf(CoreException.class)
                    .satisfies(ex -> assertThat(((CoreException) ex).getErrorType()).isEqualTo(ErrorType.CONFLICT));
        }
    }
}
