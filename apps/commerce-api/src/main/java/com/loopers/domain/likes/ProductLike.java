package com.loopers.domain.likes;

import com.loopers.domain.BaseEntity;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_like")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLike extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_user_id")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_product_id")
    private ProductEntity productEntity;

    @Builder
    private ProductLike(UserEntity userEntity, ProductEntity productEntity) {
        this.userEntity = userEntity;
        this.productEntity = productEntity;
    }


    public static ProductLike create(UserEntity user, ProductEntity product) {
        return ProductLike
                .builder()
                .userEntity(user)
                .productEntity(product)
                .build();
    }


}
