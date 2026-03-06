package com.loopers.interfaces.api.category;

import com.loopers.application.category.CategoryFacade;
import com.loopers.domain.category.CategoryInfo;
import com.loopers.domain.product.ProductSort;
import com.loopers.domain.product.ProductsInfo;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.product.ProductV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryV1Controller implements CategoryV1ApiSpec {

    private final CategoryFacade categoryFacade;

    @GetMapping
    @Override
    public ApiResponse<CategoryV1Dto.Response.CategoryTree> getCategories() {
        List<CategoryInfo> categoryTree = categoryFacade.getCategoryTree();
        return ApiResponse.success(CategoryV1Dto.Response.CategoryTree.from(categoryTree));
    }

    @GetMapping("/{categoryId}/products")
    @Override
    public ApiResponse<ProductV1Dto.Response.Summary> getProductsByCategoryId(
            @PathVariable Long categoryId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam ProductSort productSort) {
        ProductsInfo productsInfo = categoryFacade.getProductsByCategoryId(categoryId, page, size, productSort);
        return ApiResponse.success(ProductV1Dto.Response.Summary.from(productsInfo));
    }
}
