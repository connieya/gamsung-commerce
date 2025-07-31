package com.loopers.domain.product;

import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.brand.Brand;
import com.loopers.domain.product.brand.BrandRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductLikeRepository productLikeRepository;

    @Test
    @DisplayName("상품 목록 조회시 브랜드 정보와 좋아요 개수를 함께 조회한다.")
    @Transactional
    void findProductDetails() {
        // given
        User user1 = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User user2 = UserFixture.complete().set(Select.field(User::getUserId), "cony").create();
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);


        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product productA = ProductFixture.complete()
                .set(Select.field(Product::getName), "상품A")
                .set(Select.field(Product::getPrice), 50000L)
                .create();

        Product productB = ProductFixture.complete()
                .set(Select.field(Product::getName), "상품B")
                .set(Select.field(Product::getPrice), 70000L)
                .create();

        Product savedProductA = productRepository.save(productA, savedBrand.getId());
        Product savedProductB = productRepository.save(productB, savedBrand.getId());


        productLikeRepository.save(savedUser1.getId(), savedProductA.getId());
        productLikeRepository.save(savedUser1.getId(), savedProductB.getId());
        productLikeRepository.save(savedUser2.getId(), savedProductB.getId());

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductInfo> productDetails = productRepository.findProductDetails(pageable);
        List<ProductInfo> content = productDetails.getContent();

        // then
        assertThat(content).hasSize(2)
                .extracting("productId", "productName", "price", "brandName", "likeCount")
                .containsExactlyInAnyOrder(
                        tuple(savedProductA.getId(), "상품A", 50000L, savedBrand.getName(), 1L),
                        tuple(savedProductB.getId(), "상품B", 70000L, savedBrand.getName(), 2L)
                );
    }
}
