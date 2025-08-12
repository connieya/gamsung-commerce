package com.loopers.infrastructure.likes;

import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.user.exception.UserException;
import com.loopers.domain.likes.ProductLike;
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
    private final UserJpaRepository userJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    @Override
    public ProductLike save(Long userId, Long productId) {
        UserEntity userEntity = userJpaRepository.findById(userId).orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));
        ProductEntity productEntity = productJpaRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));

        return productLikeJpaRepository.save(ProductLikeEntity.from(userEntity, productEntity))
                .toDomain();
    }

    @Override
    public boolean existsByUserIdAndProductId(Long userId , Long productId) {
        return productLikeJpaRepository.existsByUserEntity_IdAndProductEntity_Id(userId, productId);
    }

    @Override
    public Long getLikeCount(Long productId) {
        ProductEntity productEntity = productJpaRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));
        return productLikeJpaRepository.countByProductEntity(productEntity);
    }

    @Override
    public void delete(Long userId, Long productId) {
        productLikeJpaRepository.deleteByUserEntity_IdAndProductEntity_Id(userId ,productId);
    }

    @Override
    public List<ProductLike> findByUserId(Long id) {
        return productLikeJpaRepository.findByUserEntity_Id(id)
                .stream()
                .map(ProductLikeEntity::toDomain)
                .toList();
    }
}

