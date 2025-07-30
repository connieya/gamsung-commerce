package com.loopers.application.likes;

import com.loopers.domain.likes.ProductLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;

    public void add(Long userId, Long productId) {
        boolean existed = productLikeRepository.existsByUserIdAndProductId(userId, productId);

        if (!existed) {
            productLikeRepository.save(userId,productId);
        }
    }

    public void remove(Long userId, Long productId) {

    }
}
