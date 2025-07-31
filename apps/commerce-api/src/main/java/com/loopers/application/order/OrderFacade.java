package com.loopers.application.order;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderFacade {

    ProductService productService;

    public void place(OrderCriteria orderCriteria) {
        List<Product> products = productService.findAllById(orderCriteria.getProductIds());
    }
}
