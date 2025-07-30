package com.loopers.infrastructure.likes;

import com.loopers.infrastructure.product.ProductEntity;
import org.springframework.data.repository.CrudRepository;

public interface ProductLikeJpaRepository extends CrudRepository<ProductLikeEntity , Long> {

    boolean existsByUserEntity_IdAndProductEntity_Id(Long userId, Long productId);

    Long countByProductEntity(ProductEntity productEntity);
}

