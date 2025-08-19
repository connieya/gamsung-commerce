package com.loopers.domain.brand;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class BrandCommand {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UpdateBrand {
        private Long brandId;
        private String name;
        private String description;
    }
}
