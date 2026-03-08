package com.loopers.interfaces.api.internal;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @PostMapping("/bulk")
    public ApiResponse<List<InternalDto.ProductResponse>> getProducts(@RequestBody InternalDto.ProductBulkRequest request) {
        List<Product> products = productService.findAllById(request.productIds());
        List<InternalDto.ProductResponse> response = products.stream()
                .map(p -> new InternalDto.ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getImageUrl()))
                .toList();
        return ApiResponse.success(response);
    }
}
