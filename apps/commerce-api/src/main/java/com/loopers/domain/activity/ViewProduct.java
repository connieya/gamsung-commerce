package com.loopers.domain.activity;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "view_product")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ViewProduct extends BaseEntity {

    @Column(name = "ref_product_id" , nullable = false)
    private Long productId;

    @Column(name = "view_count" , nullable = false)
    private Long viewCount;

    @Builder
    private ViewProduct(Long productId, Long viewCount) {
        this.productId = productId;
        this.viewCount = viewCount;
    }

    public static ViewProduct create(Long productId, Long viewCount) {
        return ViewProduct
                .builder()
                .productId(productId)
                .viewCount(viewCount)
                .build();
    }

    public void view() {
        this.viewCount++;
    }

}
