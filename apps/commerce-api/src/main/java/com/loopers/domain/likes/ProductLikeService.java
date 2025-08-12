package com.loopers.domain.likes;

import com.loopers.domain.likes.exception.LikeException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductLikeService {

    private final ProductLikeRepository productLikeRepository;
    private final LikeSummaryRepository likeSummaryRepository;

    @Transactional
    public void add(Long userId, Long productId) {
        boolean existed = productLikeRepository.existsByUserIdAndProductId(userId, productId);
        if (existed) {
            return;
        }
        LikeSummary likeSummary = likeSummaryRepository.findByTarget(LikeTarget.create(productId, LikeTargetType.PRODUCT)).orElseGet(() ->
                likeSummaryRepository.save(LikeSummary.create(productId, LikeTargetType.PRODUCT))
        );

        productLikeRepository.save(userId, productId);
        likeSummary.increase();

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
