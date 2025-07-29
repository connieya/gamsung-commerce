package com.loopers.infrastructure.likes;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.likes.ProductLike;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_like")
public class ProductLikeEntity extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_user_id")
    private UserEntity userEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_product_id")
    private ProductEntity productEntity;

    public static ProductLikeEntity fromDomain(ProductLike productLike) {
        ProductLikeEntity productLikeEntity = new ProductLikeEntity();

        productLikeEntity.userEntity = UserEntity.fromDomain(productLike.getUser());
        productLikeEntity.productEntity = ProductEntity.fromDomain(productLike.getProduct());

        return productLikeEntity;
    }

    public ProductLike toDomain() {
        return ProductLike.builder()
                .user(userEntity.toDomain())
                .product(productEntity.toDomain())
                .build();
    }


}
