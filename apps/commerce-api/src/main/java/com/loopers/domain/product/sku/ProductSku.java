package com.loopers.domain.product.sku;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_sku")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSku extends BaseEntity {

    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;

    @Column(name = "additional_price", nullable = false)
    private Long additionalPrice;

    @OneToMany(mappedBy = "sku", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductSkuOption> skuOptions = new ArrayList<>();

    @Builder
    private ProductSku(Long productId, String skuCode, Long additionalPrice) {
        this.productId = productId;
        this.skuCode = skuCode;
        this.additionalPrice = additionalPrice;
    }

    public static ProductSku create(Long productId, String skuCode, Long additionalPrice) {
        return ProductSku.builder()
                .productId(productId)
                .skuCode(skuCode)
                .additionalPrice(additionalPrice)
                .build();
    }

    public void addOption(ProductSkuOption skuOption) {
        this.skuOptions.add(skuOption);
    }
}
