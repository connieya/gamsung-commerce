package com.loopers.domain.category.fixture;

import com.loopers.domain.category.Category;

public class CategoryFixture {

    public static Category createRoot() {
        return Category.createRoot("상의", 1);
    }

    public static Category createChild(Category parent) {
        return Category.createChild("반소매 티셔츠", parent, 1);
    }
}
