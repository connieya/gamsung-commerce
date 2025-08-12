package com.loopers.domain.product;

import org.springframework.data.domain.Sort;

public enum ProductSort {
    LATEST_DESC("releasedAt", Sort.Direction.DESC),
    LATEST_ASC("releasedAt", Sort.Direction.ASC),
    LIKES_ASC("likeCount", Sort.Direction.ASC),
    LIKES_DESC("likeCount", Sort.Direction.DESC),
    PRICE_ASC("price", Sort.Direction.ASC),
    PRICE_DESC("price", Sort.Direction.DESC);

    private final String property;
    private final Sort.Direction direction;

    ProductSort(String property, Sort.Direction direction) {
        this.property = property;
        this.direction = direction;
    }

    public Sort toSort() {
        return Sort.by(direction, property);
    }
}
