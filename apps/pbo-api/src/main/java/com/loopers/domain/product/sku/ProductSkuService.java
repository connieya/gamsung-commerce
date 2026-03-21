package com.loopers.domain.product.sku;

import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.sku.exception.SkuException;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductSkuService {

    private final ProductSkuRepository productSkuRepository;

    @Transactional
    public ProductSku createSku(ProductSkuCommand.Create command) {
        List<ProductOption> options = command.validatedOptions();

        boolean allBelongToProduct = options.stream()
                .allMatch(option -> option.getProductId().equals(command.productId()));
        if (!allBelongToProduct) {
            throw new CoreException(ErrorType.BAD_REQUEST, "옵션이 해당 상품에 속하지 않습니다.");
        }

        if (productSkuRepository.existsBySkuCode(command.skuCode())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 사용 중인 SKU 코드입니다.");
        }

        List<ProductSku> existingSkus = productSkuRepository.findByProductId(command.productId());
        Set<Long> newOptionIds = new HashSet<>(command.optionIds());

        boolean isDuplicate = existingSkus.stream()
                .anyMatch(sku -> {
                    Set<Long> existingOptionIds = sku.getSkuOptions().stream()
                            .map(ProductSkuOption::getOptionId)
                            .collect(Collectors.toSet());
                    return existingOptionIds.equals(newOptionIds);
                });

        if (isDuplicate) {
            throw new SkuException.DuplicateOptionCombinationException(ErrorType.DUPLICATE_SKU_OPTION_COMBINATION);
        }

        ProductSku sku = ProductSku.create(command.productId(), command.skuCode(), command.additionalPrice());
        for (Long optionId : command.optionIds()) {
            sku.addOption(ProductSkuOption.create(sku, optionId));
        }

        return productSkuRepository.save(sku);
    }
}
