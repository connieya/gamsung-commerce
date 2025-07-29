package com.loopers.application.likes;

import com.loopers.application.product.exception.ProductException;
import com.loopers.application.user.exception.UserException;
import com.loopers.domain.likes.ProductLike;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductLikeRepository productLikeRepository;

    public void add(Long userId, Long productId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));

        ProductLike productLike = ProductLike.create(user, product);

        boolean existed = productLikeRepository.existsByUserIdAndProductId(user, product);

        if (!existed) {
            productLikeRepository.save(productLike);
        }

    }
}
