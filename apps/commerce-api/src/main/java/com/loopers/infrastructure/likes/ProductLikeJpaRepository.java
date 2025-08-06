package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.ProductLike;
import com.loopers.infrastructure.product.ProductEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductLikeJpaRepository extends CrudRepository<ProductLikeEntity , Long> {

    boolean existsByUserEntity_IdAndProductEntity_Id(Long userId, Long productId);

    Long countByProductEntity(ProductEntity productEntity);

    void deleteByUserEntity_IdAndProductEntity_Id(Long userId, Long productId);

    Optional<ProductLikeEntity> findByUserEntity_Id(Long userEntityId);
}

