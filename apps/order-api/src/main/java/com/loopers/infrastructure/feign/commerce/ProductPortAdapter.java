package com.loopers.infrastructure.feign.commerce;

import com.loopers.domain.cart.port.ProductPort;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductPortAdapter implements ProductPort {

    private final CommerceApiClient commerceApiClient;

    @Override
    public ProductInfo getProduct(Long productId) {
        List<CommerceApiDto.ProductResponse> products = commerceApiClient
                .getProducts(new CommerceApiDto.ProductBulkRequest(List.of(productId))).data();

        return products.stream()
                .findFirst()
                .map(p -> new ProductInfo(p.id(), p.name(), p.price(), p.imageUrl()))
                .orElseThrow(() -> new CoreException(ErrorType.PRODUCT_NOT_FOUND));
    }
}
