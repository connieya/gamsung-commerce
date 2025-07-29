package com.loopers.infrastructure.likes;

import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import org.springframework.data.repository.CrudRepository;

public interface ProductLikeJpaRepository extends CrudRepository<ProductLikeEntity , Long> {
    boolean existsByUserEntityAndProductEntity(UserEntity userEntity, ProductEntity productEntity);
}
