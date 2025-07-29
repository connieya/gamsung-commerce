package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.ProductLike;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProductLikeRepositoryImpl implements ProductLikeRepository {

    private final ProductLikeJpaRepository productLikeJpaRepository;

    @Override
    public ProductLike save(ProductLike productLike) {
        return productLikeJpaRepository.save(ProductLikeEntity.fromDomain(productLike))
                .toDomain();
    }

    @Override
    public boolean existsByUserIdAndProductId(User user, Product product) {
        return productLikeJpaRepository.existsByUserEntityAndProductEntity(UserEntity.fromDomain(user), ProductEntity.fromDomain(product));
    }
}

