package com.loopers.application.likes;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.domain.brand.Brand;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@SpringBootTest
class GetLikeProductUseCaseTest {

    @Autowired
    GetLikeProductUseCase getLikeProductUseCase;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductLikeRepository productLikeRepository;

    @Autowired
    BrandRepository brandRepository;

    @DisplayName("내가 좋아요 한 상품 목록을 조회한다.")
    @Transactional
    @Test
    void getLikeProducts() {
        // given
        User user1 = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User user2 = UserFixture.complete().set(Select.field(User::getUserId), "cony").create();
        User user3 = UserFixture.complete().set(Select.field(User::getUserId), "loopers").create();

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);
        User savedUser3 = userRepository.save(user3);

        Brand savedBrand = brandRepository.save(Brand.create("nike", "just do it"));

        Product product1 = ProductFixture.complete().set(Select.field(Product::getName), "foo1").create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getName), "foo2").create();
        Product product3 = ProductFixture.complete().set(Select.field(Product::getName), "foo3").create();

        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());
        Product savedProduct3 = productRepository.save(product3, savedBrand.getId());


        // "gunny" 라는 아이디를 가진 유저가 상품1, 상품2, 상품 3에 좋아요 등록
        Stream.of(savedProduct1, savedProduct2, savedProduct3)
                .forEach(product ->
                        productLikeRepository.save(savedUser1.getId(), product.getId())
                );

        // cony , loopers 아이디를 가진 유저가 상품 2에 좋아요 등록
        Stream.of(savedUser2, savedUser3).forEach(
                user -> {
                    productLikeRepository.save(user.getId(), savedProduct2.getId());
                }
        );

        //  loopers 아이드를 가진 유저가 상품 3에 좋아요 등록
        productLikeRepository.save(savedUser3.getId(), savedProduct3.getId());
        // 상품 1의 좋아요 개수 1개 (gunny) , 상품 2의 좋아요 개수 3개 (gunny , cony ,loopers ) , 상품 3의 좋아요 개수 2개 (gunny , loopers)


        // when
        String userId = "gunny";

        GetLikeProductResult likedProducts = getLikeProductUseCase.getLikedProducts(userId);

        // then

        assertThat(likedProducts.getProductDetailInfos()).hasSize(3)
                .extracting("productId", "productName" ,"productPrice" ,"brandName" ,"likeCount")
                .containsExactlyInAnyOrder(
                        tuple(savedProduct1.getId(),"foo1",savedProduct1.getPrice(),"nike" ,1L),
                        tuple(savedProduct2.getId(),"foo2",savedProduct2.getPrice(),"nike" ,3L),
                        tuple(savedProduct3.getId(),"foo3",savedProduct3.getPrice(),"nike" ,2L)
                );


    }

}
