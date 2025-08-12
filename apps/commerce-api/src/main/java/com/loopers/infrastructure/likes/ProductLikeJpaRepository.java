package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.ProductLike;
import com.loopers.infrastructure.product.ProductEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProductLikeJpaRepository extends CrudRepository<ProductLike, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    List<ProductLike> findByUserId(Long id);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    Long countByProductId(Long productId);
}

