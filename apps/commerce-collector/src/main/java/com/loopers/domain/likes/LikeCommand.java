package com.loopers.domain.likes;

import java.util.List;

public record LikeCommand() {

    public record Update(
            List<Item> items
    ) {
        public record Item(
                String eventId,
                Long productId,
                LikeUpdateType updateType
        ) {
            public static Item of(String eventId , Long productId , LikeUpdateType updateType) {
                return  new Item(eventId, productId, updateType);
            }
        }
    }
}
