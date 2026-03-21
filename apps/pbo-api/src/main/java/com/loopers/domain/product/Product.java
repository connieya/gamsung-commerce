package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * pbo-api에서 상품 존재 여부 확인용 최소 엔티티.
 * product 테이블 읽기 전용(existsById).
 */
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    public static Product create() {
        return new Product();
    }
}
