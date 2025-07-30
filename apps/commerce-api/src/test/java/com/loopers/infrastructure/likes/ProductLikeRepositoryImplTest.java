package com.loopers.infrastructure.likes;

import com.loopers.domain.likes.ProductLikeRepository;
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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
class ProductLikeRepositoryImplTest {

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
        Assertions.assertThat(likeCount).isEqualTo(1L);
    }

}
