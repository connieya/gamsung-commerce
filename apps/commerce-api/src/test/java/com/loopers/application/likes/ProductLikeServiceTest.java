package com.loopers.application.likes;

import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.likes.ProductLikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductLikeServiceTest {

    @InjectMocks
    ProductLikeService sut;

    @Mock
    ProductLikeRepository productLikeRepository;


    @Test
    @DisplayName("좋아요: 요청 시 성공적으로 등록된다.")
    void add_successfullyRegistersLike() {
        // given
        Long productId = 1L;
        Long userId = 1L;

        // when
        sut.add(userId, productId);

        // then
        verify(productLikeRepository, times(1)).save(userId, productId);
    }


    @Test
    @DisplayName("좋아요: 이미 등록된 경우, 중복 저장되지 않아 멱등성이 보장된다.")
    void add_ensuresIdempotency_onDuplicate() {
        // given
        Long productId = 1L;
        Long userId = 1L;

        when(productLikeRepository.existsByUserIdAndProductId(userId, productId))
                .thenReturn(true);

        // when
        sut.add(userId, productId);

        // then
        verify(productLikeRepository, never()).save(userId, productId);
    }

    @Test
    @DisplayName("좋아요: 요청 시 성공적으로 삭제된다.")
    void remove_successfullyDeletesLike() {
        // given
        Long userId = 1L;
        Long productId = 1L;

        // when
        sut.remove(userId, productId);

        // then.
        verify(productLikeRepository, times(1)).delete(userId, productId);
    }

}
