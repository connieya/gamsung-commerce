package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.ProductLike;
import com.loopers.infrastructure.product.ProductEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductLikeJpaRepository extends CrudRepository<ProductLike, Long> {

    boolean existsByUserEntity_IdAndProductEntity_Id(Long userId, Long productId);

    Long countByProductEntity(ProductEntity productEntity);

    void deleteByUserEntity_IdAndProductEntity_Id(Long userId, Long productId);

    List<ProductLike> findByUserEntity_Id(Long userEntityId);
}

