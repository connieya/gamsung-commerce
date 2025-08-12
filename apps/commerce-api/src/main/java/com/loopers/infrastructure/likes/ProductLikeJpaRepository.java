package com.loopers.infrastructure.likes;

import com.loopers.infrastructure.product.ProductEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductLikeJpaRepository extends CrudRepository<ProductLikeEntity , Long> {

    boolean existsByUserEntity_IdAndProductEntity_Id(Long userId, Long productId);

    Long countByProductEntity(ProductEntity productEntity);

    void deleteByUserEntity_IdAndProductEntity_Id(Long userId, Long productId);

    List<ProductLikeEntity> findByUserEntity_Id(Long userEntityId);
}

