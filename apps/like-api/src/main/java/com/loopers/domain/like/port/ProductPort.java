package com.loopers.domain.like.port;

import java.util.List;

public interface ProductPort {

    List<ProductInfo> getProducts(List<Long> productIds);

    record ProductInfo(Long id, String name, Long price, String imageUrl, String brandName) {}
}
