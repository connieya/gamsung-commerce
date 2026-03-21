package com.loopers.application.sku;

import com.loopers.domain.product.option.ProductOption;
import com.loopers.domain.product.option.ProductOptionService;
import com.loopers.domain.product.sku.ProductSku;
import com.loopers.domain.product.sku.ProductSkuOption;
import com.loopers.domain.product.sku.ProductSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuFacade {

    private final ProductOptionService productOptionService;
    private final ProductSkuService productSkuService;

    @Transactional(readOnly = true)
    public SkuResult.OptionList getOptions(Long productId) {
        List<ProductOption> options = productOptionService.getOptionsByProductId(productId);
        return SkuResult.OptionList.from(options);
    }

    @Transactional(readOnly = true)
    public SkuResult.Sku getSku(Long skuId) {
        ProductSku sku = productSkuService.getSku(skuId);
        List<Long> optionIds = sku.getSkuOptions().stream()
                .map(ProductSkuOption::getOptionId)
                .toList();
        List<ProductOption> options = productOptionService.findAllByIdIn(optionIds);
        return SkuResult.Sku.from(sku, options);
    }

    @Transactional(readOnly = true)
    public SkuResult.SkuList getSkusByProduct(Long productId) {
        List<ProductSku> skus = productSkuService.getSkusByProductId(productId);
        return SkuResult.SkuList.from(skus, sku -> {
            List<Long> optionIds = sku.getSkuOptions().stream()
                    .map(ProductSkuOption::getOptionId)
                    .toList();
            return productOptionService.findAllByIdIn(optionIds);
        });
    }
}
