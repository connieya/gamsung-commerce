package com.loopers.domain.product;

import com.loopers.domain.product.exception.ProductException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public void assertExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND);
        }
    }
}
