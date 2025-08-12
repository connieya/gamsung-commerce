package com.loopers.domain.likes;

import java.util.List;

public interface ProductLikeRepository {

    ProductLike save(Long userId , Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    Long getLikeCount(Long productId);

    void delete(Long userId , Long productId);

    List<ProductLike> findByUserId(Long id);
}
