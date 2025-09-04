package com.loopers.domain.likes;

import com.loopers.domain.likes.event.ProductLikeEvent;
import com.loopers.domain.likes.exception.LikeException;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.exception.UserException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void add(Long userId, Long productId) {
        userRepository.findById(userId).orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));
        productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));

        boolean existed = productLikeRepository.existsByUserIdAndProductId(userId, productId);
        if (existed) {
            return;
        }
        productLikeRepository.save(userId, productId);
        applicationEventPublisher.publishEvent(ProductLikeEvent.Update.of(productId, ProductLikeEvent.Update.UpdateType.INCREMENT));
    }

    @Transactional
    public void remove(Long userId, Long productId) {
        userRepository.findById(userId).orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));
        productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));

        boolean existed = productLikeRepository.existsByUserIdAndProductId(userId, productId);
        if (existed) {
            productLikeRepository.delete(userId, productId);
            applicationEventPublisher.publishEvent(ProductLikeEvent.Update.of(productId, ProductLikeEvent.Update.UpdateType.INCREMENT));
        }
    }
}
