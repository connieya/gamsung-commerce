package com.loopers.application.likes;

import com.loopers.domain.likes.ProductLikeRepository;
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
    @DisplayName("좋아요: 중복 요청 시에도 한 번만 저장되어 멱등성이 보장된다.")
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

}
