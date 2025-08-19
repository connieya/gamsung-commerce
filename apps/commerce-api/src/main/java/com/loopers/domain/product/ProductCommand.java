package com.loopers.domain.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductCommand {

    @Getter
    public static class Register {
        private String name;
        private Long price;
        private Long brandId;

        @Builder
        private Register(String name, Long price, Long brandId) {
            this.name = name;
            this.price = price;
            this.brandId = brandId;
        }

        public static Register create(String name, Long price, Long brandId) {
            return Register
                    .builder()
                    .name(name)
                    .price(price)
                    .brandId(brandId)
                    .build();
        }
    }

    @Getter
    public static class Search {
        private int page;
        private int size;
        private ProductSort productSort;
        private Long brandId;

        @Builder
        private Search(int page, int size, ProductSort productSort, Long brandId) {
            this.page = page;
            this.size = size;
            this.productSort = productSort;
            this.brandId = brandId;
        }

        public static Search create(int page, int size, ProductSort productSort, Long brandId) {
            return Search
                    .builder()
                    .page(page)
                    .size(size)
                    .productSort(productSort)
                    .brandId(brandId)
                    .build();
        }
    }


}
