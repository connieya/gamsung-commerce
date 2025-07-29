package com.loopers.domain.product;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Brand {

    private String name;
    private String description;

    @Builder
    private Brand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static Brand create(String name, String description) {
        return Brand.builder()
                .name(name)
                .description(description)
                .build();
    }
}
