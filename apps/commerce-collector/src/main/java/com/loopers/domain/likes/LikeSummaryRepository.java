package com.loopers.domain.likes;


public interface LikeSummaryRepository {

    void updateLikeCountBy(Long productId, Long likeChanged);
}
