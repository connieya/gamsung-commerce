package com.loopers.application.likes;

import com.loopers.domain.likes.ProductLike;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class ProductLikeServiceTest {

    @InjectMocks
    ProductLikeService sut;

    @Mock
    ProductLikeRepository productLikeRepository;


    @Test
    @DisplayName("좋아요 등록에 성공한다.")
    void like_add_success() {
        // given
        Long productId = 1L;
        Long userId = 1L;

        // when
        sut.add(userId, productId);

        Product product = ProductFixture.complete().create();
        User user = UserFixture.complete().create();

        // then
        verify(productLikeRepository, times(1)).save(ProductLike.create(user,product));
    }

}
