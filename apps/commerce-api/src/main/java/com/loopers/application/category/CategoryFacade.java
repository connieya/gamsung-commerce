package com.loopers.application.category;

import com.loopers.domain.category.CategoryInfo;
import com.loopers.domain.category.CategoryService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSort;
import com.loopers.domain.product.ProductsInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryFacade {

    private final CategoryService categoryService;
    private final ProductService productService;

    public List<CategoryInfo> getCategoryTree() {
        return categoryService.getCategoryTree();
    }

    public ProductsInfo getProductsByCategoryId(Long categoryId, int page, int size, ProductSort sortType) {
        return productService.getProductsByCategoryId(categoryId, page, size, sortType);
    }
}
