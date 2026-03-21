package com.loopers.domain.product.option;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;

    @Transactional
    public ProductOption register(ProductOptionCommand.Create command) {
        ProductOption option = ProductOption.create(
                command.productId(),
                command.optionType(),
                command.optionValue()
        );
        return productOptionRepository.save(option);
    }

    @Transactional(readOnly = true)
    public List<ProductOption> findAllByIdIn(List<Long> optionIds) {
        return productOptionRepository.findAllByIdIn(optionIds);
    }
}
