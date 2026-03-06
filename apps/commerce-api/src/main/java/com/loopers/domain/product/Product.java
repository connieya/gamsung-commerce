package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.Brand;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.ZonedDateTime;

@Entity
@Table(name = "product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    private String name;

    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_brand_id")
    private Brand brand;

    @Column(name = "ref_category_id")
    private Long categoryId;

    @Column(name = "image_url")
    private String imageUrl;

    private ZonedDateTime releasedAt;

    @Builder
    private Product(String name, Long price, Brand brand, Long categoryId, String imageUrl, ZonedDateTime releasedAt) {
        if (!StringUtils.hasText(name)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이름이 올바르지 않습니다.");
        }

        if (price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "기본 가격은 0 이상이어야 합니다.");
        }

        if (brand == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드는 필수입니다.");
        }

        if (categoryId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "카테고리는 필수입니다.");
        }

        if (releasedAt == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "출시일은 필수 값입니다.");
        }

        this.name = name;
        this.price = price;
        this.brand = brand;
        this.categoryId = categoryId;
        this.imageUrl = imageUrl;
        this.releasedAt = releasedAt;
    }

    public static Product create(String name, Long price, Brand brand, Long categoryId, String imageUrl, ZonedDateTime releasedAt) {
        return Product.builder()
                .name(name)
                .price(price)
                .brand(brand)
                .categoryId(categoryId)
                .imageUrl(imageUrl)
                .releasedAt(releasedAt)
                .build();
    }

    public Long getBrandId() {
        return brand != null ? brand.getId() : null;
    }
}
