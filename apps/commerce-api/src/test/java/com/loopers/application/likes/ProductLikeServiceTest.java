package com.loopers.application.likes;

import com.loopers.domain.likes.ProductLike;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductLikeServiceTest {

    @InjectMocks
    ProductLikeService sut;

    @Mock
    ProductLikeRepository productLikeRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ProductRepository productRepository;


    @Test
    @DisplayName("좋아요: 요청 시 성공적으로 등록된다.")
    void add_successfullyRegistersLike() {
        // given
        Long productId = 1L;
        Long userId = 1L;

        Product product = ProductFixture.complete().create();
        User user = UserFixture.complete().create();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // when
        sut.add(userId, productId);

        // then
        verify(productLikeRepository, times(1)).save(ProductLike.create(user,product));
    }


    @Test
    @DisplayName("좋아요: 중복 요청 시에도 한 번만 저장되어 멱등성이 보장된다.")
    void add_ensuresIdempotency_onDuplicate() {
        // given
        Long productId = 1L;
        Long userId = 1L;

        Product product = ProductFixture.complete().create();
        User user = UserFixture.complete().create();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productLikeRepository.existsByUserIdAndProductId(user, product))
                .thenReturn(true);

        // when
        sut.add(userId, productId);

        // then
        verify(productLikeRepository, never()).save(ProductLike.create(user, product));
    }

}
