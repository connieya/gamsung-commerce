package com.loopers.domain.likes;

import com.loopers.infrastructure.likes.LikeSummaryJpaRepository;
import com.loopers.infrastructure.likes.ProductLikeJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;


import java.util.Optional;

import static org.mockito.Mockito.*;


@SpringBootTest
class ProductLikeServiceTest {

    @Autowired
    ProductLikeService productLikeService;

    @MockitoSpyBean
    ProductLikeJpaRepository productLikeJpaRepository;

    @MockitoSpyBean
    LikeSummaryJpaRepository likeSummaryJpaRepository;



    @Test
    @DisplayName("좋아요: 요청 시 성공적으로 등록된다.")
    void add_successfullyRegistersLike() {
        // given
        Long productId = 1L;
        Long userId = 1L;

        // when
        doReturn(false)
                .when(productLikeJpaRepository)
                .existsByUserIdAndProductId(userId, productId);

        productLikeService.add(userId, productId);

        // then

        verify(productLikeJpaRepository, times(1)).save(any(ProductLike.class));

        verify(likeSummaryJpaRepository, times(1)).save(any(LikeSummary.class));
    }


    @Test
    @DisplayName("좋아요: 이미 등록된 경우, 중복 저장되지 않아 멱등성이 보장된다.")
    void add_ensuresIdempotency_onDuplicate() {
        // given
        Long productId = 1L;
        Long userId = 1L;

        doReturn(true)
                .when(productLikeJpaRepository)
                .existsByUserIdAndProductId(userId, productId);
        // when
        productLikeService.add(userId, productId);

        // then
        verify(productLikeJpaRepository, never()).save(any(ProductLike.class));
    }

    @Test
    @DisplayName("좋아요: 요청 시 성공적으로 삭제된다.")
    void remove_successfullyDeletesLike() {
        // given
        Long userId = 1L;
        Long productId = 1L;
        LikeSummary mockLikeSummary = mock(LikeSummary.class);
        // when
        doReturn(true)
                .when(productLikeJpaRepository)
                .existsByUserIdAndProductId(userId, productId);

        doReturn(Optional.of(mockLikeSummary))
                .when(likeSummaryJpaRepository).findByTarget(any(LikeTarget.class));
        productLikeService.remove(userId, productId);

        // then
        verify(productLikeJpaRepository, times(1)).deleteByUserIdAndProductId(userId, productId);
        verify(likeSummaryJpaRepository, times(1)).findByTarget(LikeTarget.create(productId, LikeTargetType.PRODUCT));
        verify(mockLikeSummary, times(1)).decrease();
    }

}
