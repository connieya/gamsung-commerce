package com.loopers.domain.metrics;

import jakarta.annotation.Nullable;

import java.time.LocalDate;
import java.util.List;

public record MetricCommand() {

    public record Aggregate(
            List<Item> items
    ) {
        public record Item(
                String eventId,
                LocalDate date,
                Long productId,
                @Nullable Long likeCount,
                @Nullable Long saleQuantity,
                @Nullable Long viewCount
        ) {
            public static Item ofViewCount(String eventId, LocalDate date, Long productId, Long viewCount) {
                return new Item(eventId, date, productId, 0L, 0L, viewCount);
            }

            public static Item ofLikeCount(String eventId, LocalDate date, Long productId, Long likeCount) {
                return new Item(eventId, date, productId, likeCount, 0L, 0L);
            }

            public static Item ofSaleCount(String eventId, LocalDate date, Long productId, Long saleQuantity) {
                return new Item(eventId, date, productId, 0L, saleQuantity, 0L);
            }


        }
    }
}
