package com.loopers.domain.likes;

import com.loopers.domain.likes.exception.LikeException;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.exception.ProductException;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.exception.UserException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;
    private final LikeSummaryRepository likeSummaryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void add(Long userId, Long productId) {
        userRepository.findById(userId).orElseThrow(() -> new UserException.UserNotFoundException(ErrorType.USER_NOT_FOUND));
        productRepository.findById(productId).orElseThrow(() -> new ProductException.ProductNotFoundException(ErrorType.PRODUCT_NOT_FOUND));

        boolean existed = productLikeRepository.existsByUserIdAndProductId(userId, productId);
        if (existed) {
            return;
        }
        System.out.println("userId = " + userId);
        productLikeRepository.save(userId, productId);
        Optional<LikeSummary> byTarget = likeSummaryRepository.findByTargetUpdate(LikeTarget.create(productId, LikeTargetType.PRODUCT));
        if (byTarget.isPresent()) {
            System.out.println("11111");
            LikeSummary likeSummary = byTarget.get();
            likeSummary.increase();
        }else {
            System.out.println("22222");
            LikeSummary likeSummary = LikeSummary.create(productId, LikeTargetType.PRODUCT);
            likeSummary.increase();
            likeSummaryRepository.save(likeSummary);
        }
//        likeSummaryRepository.findByTarget(LikeTarget.create(productId, LikeTargetType.PRODUCT))
//                .ifPresentOrElse(
//                        LikeSummary::increase,
//                        () -> likeSummaryRepository.save(LikeSummary.create(productId, LikeTargetType.PRODUCT))
//                );

    }

    @Transactional
    public void remove(Long userId, Long productId) {
        boolean existed = productLikeRepository.existsByUserIdAndProductId(userId, productId);
        if (existed) {
            productLikeRepository.delete(userId, productId);
            LikeSummary likeSummary = likeSummaryRepository.findByTarget(LikeTarget.create(productId, LikeTargetType.PRODUCT))
                    .orElseThrow(() -> new LikeException.LikeSummaryNotFoundException(ErrorType.LIKE_SUMMARY_NOT_FOUND));
            likeSummary.decrease();
        }
    }
}
