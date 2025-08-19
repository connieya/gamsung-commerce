package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.ProductLike;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.user.exception.UserException;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserEntity;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class ProductLikeCoreRepository implements ProductLikeRepository {

    private final ProductLikeJpaRepository productLikeJpaRepository;

    @Override
    public ProductLike save(Long userId, Long productId) {
        return productLikeJpaRepository.save(ProductLike.create(userId, productId));
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId , Long productId) {
        return productLikeJpaRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public Long getLikeCount(Long productId) {
        return productLikeJpaRepository.countByProductId(productId);
    }

    @Override
    public void delete(Long userId, Long productId) {
        productLikeJpaRepository.deleteByUserIdAndProductId(userId ,productId);
    }

    @Override
    public List<ProductLike> findByUserId(Long id) {
        return productLikeJpaRepository.findByUserId(id);
    }
}

