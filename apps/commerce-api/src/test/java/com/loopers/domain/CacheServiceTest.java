package com.loopers.domain;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.likes.ProductLikeService;
import com.loopers.domain.product.*;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CacheServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductLikeService productLikeService;

    @Autowired
    private ProductService productService;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    // FIXME 임시 테스트
    @Test
    void method() {
        // given
        User user1 = UserFixture.complete().create();
        User savedUser1 = userRepository.save(user1);

        User user2 = UserFixture.complete().create();
        User savedUser2 = userRepository.save(user2);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product = ProductFixture.complete().create();
        Product register = productService.register(ProductCommand.Register.create(product.getName(), product.getPrice(), savedBrand.getId()));

        // when
        ProductDetailInfo productDetailInfo = productService.getProductDetail(register.getId());

        productLikeService.add(savedUser1.getId(), register.getId());
        productLikeService.add(savedUser2.getId(), register.getId());
        productLikeService.remove(savedUser2.getId(), register.getId());
    }
}
