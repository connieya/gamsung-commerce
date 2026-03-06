package com.loopers.domain.category;

import java.util.List;

public record CategoryInfo(
        Long id,
        String name,
        int displayOrder,
        List<CategoryInfo> children
) {
    public static CategoryInfo of(Category category, List<CategoryInfo> children) {
        return new CategoryInfo(
                category.getId(),
                category.getName(),
                category.getDisplayOrder(),
                children
        );
    }

    public static CategoryInfo leaf(Category category) {
        return new CategoryInfo(
                category.getId(),
                category.getName(),
                category.getDisplayOrder(),
                List.of()
        );
    }
}
