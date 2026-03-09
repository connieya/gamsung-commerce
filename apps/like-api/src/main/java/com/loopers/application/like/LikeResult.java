package com.loopers.application.like;

import java.util.List;

public class LikeResult {

    public record LikedProducts(List<LikedProductItem> items) {
        public record LikedProductItem(
                Long productId,
                String productName,
                Long productPrice,
                String brandName,
                String imageUrl,
                Long likeCount
        ) {}
    }
}
