package com.loopers.domain.product.sku;

import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.option.ProductOptionRepository;
import com.loopers.domain.product.sku.exception.SkuException;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductSkuService {

    private final ProductSkuRepository productSkuRepository;
    private final ProductOptionRepository productOptionRepository;

    @Transactional
    public ProductSku createSku(ProductSkuCommand.Create command) {
        List<ProductOption> options = productOptionRepository.findAllByIdIn(command.optionIds());

        if (options.size() != command.optionIds().size()) {
            throw new CoreException(ErrorType.OPTION_NOT_FOUND, "요청한 옵션 중 존재하지 않는 옵션이 있습니다.");
        }

        boolean allBelongToProduct = options.stream()
                .allMatch(option -> option.getProductId().equals(command.productId()));
        if (!allBelongToProduct) {
            throw new CoreException(ErrorType.BAD_REQUEST, "옵션이 해당 상품에 속하지 않습니다.");
        }

        List<ProductSku> existingSkus = productSkuRepository.findByProductId(command.productId());
        Set<Long> newOptionIds = new HashSet<>(command.optionIds());

        boolean isDuplicate = existingSkus.stream()
                .anyMatch(sku -> {
                    Set<Long> existingOptionIds = new HashSet<>();
                    sku.getSkuOptions().forEach(o -> existingOptionIds.add(o.getOptionId()));
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

    @Transactional(readOnly = true)
    public ProductSku getSku(Long skuId) {
        return productSkuRepository.findById(skuId)
                .orElseThrow(() -> new SkuException.SkuNotFoundException(ErrorType.SKU_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<ProductSku> getSkusByProductId(Long productId) {
        return productSkuRepository.findByProductId(productId);
    }
}
