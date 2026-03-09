package com.loopers.infrastructure.feign.commerce;

import com.loopers.domain.like.port.ProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductPortAdapter implements ProductPort {

    private final CommerceApiClient commerceApiClient;

    @Override
    public List<ProductInfo> getProducts(List<Long> productIds) {
        List<CommerceApiDto.ProductResponse> products = commerceApiClient
                .getProducts(new CommerceApiDto.ProductBulkRequest(productIds)).data();

        return products.stream()
                .map(p -> new ProductInfo(p.id(), p.name(), p.price(), p.imageUrl(), p.brandName()))
                .toList();
    }
}
