package com.loopers.domain.stock;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class StockCommand {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public static class DeductStocks {
        private List<Item> items;


        @Builder
        private DeductStocks(List<Item> items) {
            this.items = items;
        }

        @Getter
        @Builder
        public static class Item {
            private Long productId;
            private Long quantity;
        }

        public static DeductStocks create(List<Item> items) {
            return DeductStocks
                    .builder()
                    .items(items)
                    .build();

        }
     }


}
