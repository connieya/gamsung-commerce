package com.loopers.infrastructure.likes;

import org.springframework.data.repository.CrudRepository;

public interface ProductLikeJpaRepository extends CrudRepository<ProductLikeEntity , Long> {

    boolean existsByUserEntity_IdAndProductEntity_Id(Long userId, Long productId);

    Long countByProductEntityId(Long productId);
}

