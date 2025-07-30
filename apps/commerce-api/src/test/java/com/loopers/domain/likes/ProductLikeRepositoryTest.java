package com.loopers.domain.likes;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.brand.Brand;
import com.loopers.domain.product.brand.BrandRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class ProductLikeRepositoryTest {

    @Autowired
    ProductLikeRepository productLikeRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Test
    @DisplayName("상품 좋아요 개수 조회")
    @Transactional
    void getLikeCount() {
        // given
        Brand brand = brandRepository.save(BrandFixture.complete().create());

        Product product = ProductFixture.complete().create();
        Product savedProduct = productRepository.save(product, brand.getId());

        User user = UserFixture.complete().create();
        User savedUser = userRepository.save(user);

        // when
        productLikeRepository.save(savedUser.getId(), savedProduct.getId());


        // then
        Long likeCount = productLikeRepository.getLikeCount(savedProduct.getId());
        assertThat(likeCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("좋아요가 존재 할 때, 존재 여부를 참/거짓으로 반환한다. ")
    @Transactional
    void existsByUserIdAndProductId() {
        // given
        User user = userRepository.save(UserFixture.complete().create());
        Brand brand = brandRepository.save(BrandFixture.complete().create());
        Product product = productRepository.save(ProductFixture.complete().create(), brand.getId());

        productLikeRepository.save(user.getId(), product.getId());

        // when & then
        assertTrue(productLikeRepository.existsByUserIdAndProductId(user.getId(), product.getId()));
    }


    @Test
    @DisplayName("좋아요 취소 ")
    @Transactional
    void delete() {
        // given
        User user = userRepository.save(UserFixture.complete().create());
        Brand brand = brandRepository.save(BrandFixture.complete().create());
        Product product = productRepository.save(ProductFixture.complete().create(), brand.getId());

        productLikeRepository.save(user.getId(), product.getId());

        // when
        productLikeRepository.delete(user.getId(), brand.getId());

        // then
        assertFalse(productLikeRepository.existsByUserIdAndProductId(user.getId(), product.getId()));
    }
}
