package com.loopers.interfaces.api.category;

import com.loopers.domain.category.CategoryInfo;

import java.util.List;

public class CategoryV1Dto {
    public static class Response {
        public record CategoryTree(
                List<CategoryItem> categories
        ) {
            public static CategoryTree from(List<CategoryInfo> categoryInfos) {
                List<CategoryItem> categories = categoryInfos.stream()
                        .map(CategoryItem::from)
                        .toList();
                return new CategoryTree(categories);
            }
        }

        public record CategoryItem(
                Long id,
                String name,
                List<CategoryItem> children
        ) {
            public static CategoryItem from(CategoryInfo info) {
                List<CategoryItem> children = info.children().stream()
                        .map(CategoryItem::from)
                        .toList();
                return new CategoryItem(info.id(), info.name(), children);
            }
        }
    }
}
