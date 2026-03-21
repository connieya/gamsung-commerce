package com.loopers.application.sku;

import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.option.ProductOptionCommand;
import com.loopers.domain.product.option.ProductOptionService;
import com.loopers.domain.product.sku.ProductSku;
import com.loopers.domain.product.sku.ProductSkuCommand;
import com.loopers.domain.product.sku.ProductSkuOption;
import com.loopers.domain.product.sku.ProductSkuService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuFacade {

    private final ProductService productService;
    private final ProductOptionService productOptionService;
    private final ProductSkuService productSkuService;

    @Transactional
    public SkuResult.Option registerOption(SkuCriteria.RegisterOption criteria) {
        productService.assertExists(criteria.productId());

        ProductOption option = productOptionService.register(
                new ProductOptionCommand.Create(criteria.productId(), criteria.optionType(), criteria.optionValue())
        );
        return SkuResult.Option.from(option);
    }

    @Transactional
    public SkuResult.Sku registerSku(SkuCriteria.RegisterSku criteria) {
        productService.assertExists(criteria.productId());

        List<ProductOption> options = productOptionService.findAllByIdIn(criteria.optionIds());
        if (options.size() != criteria.optionIds().size()) {
            throw new CoreException(ErrorType.OPTION_NOT_FOUND, "요청한 옵션 중 존재하지 않는 옵션이 있습니다.");
        }

        ProductSku sku = productSkuService.createSku(
                new ProductSkuCommand.Create(
                        criteria.productId(),
                        criteria.skuCode(),
                        criteria.additionalPrice(),
                        criteria.optionIds(),
                        options
                )
        );

        List<Long> optionIds = sku.getSkuOptions().stream()
                .map(ProductSkuOption::getOptionId)
                .toList();
        return SkuResult.Sku.from(sku, options.stream()
                .filter(o -> optionIds.contains(o.getId()))
                .toList());
    }
}
