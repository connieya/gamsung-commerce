package com.loopers.domain.rank;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class RankingCommand {

    @Getter
    public static class GetProducts {
        private LocalDate date;
        private int page;
        private int size;

        @Builder
        private GetProducts(LocalDate date, int page, int size) {
            this.date = date;
            this.page = page;
            this.size = size;
        }

        public static GetProducts of(LocalDate date, int page, int size) {
            return GetProducts
                    .builder()
                    .date(date)
                    .page(page)
                    .size(size)
                    .build();
        }
    }

    @Getter
    public static class GetProduct{
        private LocalDate date;
        private Long productId;

        @Builder
        private GetProduct(LocalDate date, Long productId) {
            this.date = date;
            this.productId = productId;
        }

        public static GetProduct of(LocalDate date, Long productId) {
            return GetProduct
                    .builder()
                    .date(date)
                    .productId(productId)
                    .build();
        }
    }
}
