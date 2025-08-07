package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

    private Long id;
    private String name;
    private Long price;
    private Long brandId;
    private ZonedDateTime releasedAt;

    @Builder
    private Product(Long id, String name, Long price, Long brandId ,ZonedDateTime releasedAt) {
        if (!StringUtils.hasText(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST , "이름이 올바르지 않습니다.");
        }

        if (price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST  ,"기본 가격은 0 이상이어야 합니다.");
        }

        if (brandId == null || brandId <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 ID는 0보다 큰 값이어야 합니다.");
        }

        if (releasedAt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "출시일은 필수 값입니다.");
        }

        this.id = id;
        this.name = name;
        this.price = price;
        this.brandId = brandId;
        this.releasedAt= releasedAt;
    }

    public static Product create(String name, Long price, Long brandId , ZonedDateTime releasedAt) {
        return Product.builder()
                .name(name)
                .price(price)
                .brandId(brandId)
                .releasedAt(releasedAt)
                .build();
    }
}
