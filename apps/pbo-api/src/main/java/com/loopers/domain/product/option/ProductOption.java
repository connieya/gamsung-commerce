package com.loopers.domain.product.option;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends BaseEntity {

    @Column(name = "ref_product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "option_type", nullable = false, length = 20)
    private OptionType optionType;

    @Column(name = "option_value", nullable = false)
    private String optionValue;

    @Builder
    private ProductOption(Long productId, OptionType optionType, String optionValue) {
        this.productId = productId;
        this.optionType = optionType;
        this.optionValue = optionValue;
    }

    public static ProductOption create(Long productId, OptionType optionType, String optionValue) {
        return ProductOption.builder()
                .productId(productId)
                .optionType(optionType)
                .optionValue(optionValue)
                .build();
    }
}
