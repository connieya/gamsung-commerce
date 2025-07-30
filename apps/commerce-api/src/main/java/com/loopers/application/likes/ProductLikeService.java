package com.loopers.application.likes;

import com.loopers.domain.likes.ProductLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;

    @Transactional
    public void add(Long userId, Long productId) {
        boolean existed = productLikeRepository.existsByUserIdAndProductId(userId, productId);

        if (!existed) {
            productLikeRepository.save(userId, productId);
        }
    }

    @Transactional
    public void remove(Long userId, Long productId) {
        productLikeRepository.delete(userId, productId);
    }
}
