package com.loopers.domain.product.sku;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_sku_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSkuOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_sku_id", nullable = false)
    private ProductSku sku;

    @Column(name = "ref_option_id", nullable = false)
    private Long optionId;

    @Builder
    private ProductSkuOption(ProductSku sku, Long optionId) {
        this.sku = sku;
        this.optionId = optionId;
    }

    public static ProductSkuOption create(ProductSku sku, Long optionId) {
        return ProductSkuOption.builder()
                .sku(sku)
                .optionId(optionId)
                .build();
    }
}
