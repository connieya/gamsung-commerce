package com.loopers.domain.product;

import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTargetType;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.domain.brand.Brand;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
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

    @Autowired
    LikeSummaryRepository likeSummaryRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

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
        Pageable pageable = PageRequest.of(0, 10 , ProductSort.LIKES_DESC.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetails(pageable);
        List<ProductInfo> content = productDetails.getContent();

        // then
        assertThat(content).hasSize(2)
                .extracting("productId", "productName", "price", "brandName", "likeCount")
                .containsExactly(
                        tuple(savedProductB.getId(), "상품B", 70000L, savedBrand.getName(), 2L),
                        tuple(savedProductA.getId(), "상품A", 50000L, savedBrand.getName(), 1L)
                );
    }


    @Test
    @DisplayName("상품 목록 조회시 브랜드 정보와 좋아요 개수를 함께 조회한다. (최적화된 쿼리)")
    @Transactional
    void findProductDetailsOptimized() {
        // given
        User user1 = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User user2 = UserFixture.complete().set(Select.field(User::getUserId), "cony").create();
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);


        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "adidas").create();
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
        Pageable pageable = PageRequest.of(0, 10 ,ProductSort.PRICE_ASC.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsOptimized(pageable , savedBrand.getId());
        List<ProductInfo> content = productDetails.getContent();

        // then
        assertThat(content).hasSize(2)
                .extracting("productId", "productName", "price", "brandName", "likeCount")
                .containsExactly(
                        tuple(savedProductA.getId(), "상품A", 50000L, "adidas", 1L),
                        tuple(savedProductB.getId(), "상품B", 70000L, "adidas", 2L)
                );
    }


    @Test
    @DisplayName("특정 브랜드 상품 목록 조회 시, 좋아요 수를 기준으로 내림차순 정렬되어야 한다.")
    @Transactional
    void findProductDetails_denormalized_byBrandId_sortedByLikesDesc_returnsCorrectOrder() {
        // given
        User user1 = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User user2 = UserFixture.complete().set(Select.field(User::getUserId), "cony").create();
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);


        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "adidas").create();
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

        LikeSummary likeSummary1 = LikeSummary.create(savedProductA.getId(), LikeTargetType.PRODUCT);
        likeSummary1.increase();
        LikeSummary likeSummary2 = LikeSummary.create(savedProductB.getId(), LikeTargetType.PRODUCT);
        likeSummary2.increase();
        likeSummary2.increase();
        likeSummary2.increase();
        likeSummaryRepository.save(likeSummary1);
        likeSummaryRepository.save(likeSummary2);

        // when
        Pageable pageable = PageRequest.of(0, 10 , ProductSort.DENORMALIZED_LIKES_DESC.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsDenormalizedLikeCount(pageable , savedBrand.getId());
        List<ProductInfo> content = productDetails.getContent();

        // then
        assertThat(content).hasSize(2)
                .extracting("productId", "productName", "price", "brandName", "likeCount")
                .containsExactly(
                        tuple(savedProductB.getId(), "상품B", 70000L, "adidas", 3L),
                        tuple(savedProductA.getId(), "상품A", 50000L, "adidas", 1L)
                );
    }

    @Test
    @DisplayName("존재하지 않는 브랜드 ID로 조회 시, 상품 목록이 비어 있어야 한다.")
    @Transactional
    void findProductDetails_denormalized_byNonExistingBrandId_returnsEmptyList() {
        // given
        User user1 = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User user2 = UserFixture.complete().set(Select.field(User::getUserId), "cony").create();
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);


        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "adidas").create();
        Brand brand2 = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);
        Brand savedBrand2 = brandRepository.save(brand2);

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

        LikeSummary likeSummary1 = LikeSummary.create(savedProductA.getId(), LikeTargetType.PRODUCT);
        likeSummary1.increase();
        LikeSummary likeSummary2 = LikeSummary.create(savedProductB.getId(), LikeTargetType.PRODUCT);
        likeSummary2.increase();
        likeSummary2.increase();
        likeSummary2.increase();
        likeSummaryRepository.save(likeSummary1);
        likeSummaryRepository.save(likeSummary2);

        // when
        Pageable pageable = PageRequest.of(0, 10 , ProductSort.DENORMALIZED_LIKES_DESC.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsDenormalizedLikeCount(pageable , savedBrand2.getId());
        List<ProductInfo> content = productDetails.getContent();

        // then
        assertThat(content).hasSize(0);

    }

    @Test
    @DisplayName("브랜드 필터링 없이 조회 시, 전체 상품 목록이 좋아요 수를 기준으로 내림차순 정렬되어야 한다.")
    @Transactional
    void findProductDetails_denormalized_withoutBrandId_returnsAllProductsSortedByLikesDesc() {
        // given
        User user1 = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User user2 = UserFixture.complete().set(Select.field(User::getUserId), "cony").create();
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);


        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "adidas").create();
        Brand brand2 = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);
        Brand savedBrand2 = brandRepository.save(brand2);

        Product productA = ProductFixture.complete()
                .set(Select.field(Product::getName), "상품A")
                .set(Select.field(Product::getPrice), 50000L)
                .create();

        Product productB = ProductFixture.complete()
                .set(Select.field(Product::getName), "상품B")
                .set(Select.field(Product::getPrice), 70000L)
                .create();

        Product productC = ProductFixture.complete()
                .set(Select.field(Product::getName), "상품C")
                .set(Select.field(Product::getPrice), 90000L)
                .create();

        Product savedProductA = productRepository.save(productA, savedBrand.getId());
        Product savedProductB = productRepository.save(productB, savedBrand.getId());
        Product savedProductC = productRepository.save(productC, savedBrand2.getId());


        productLikeRepository.save(savedUser1.getId(), savedProductA.getId());
        productLikeRepository.save(savedUser1.getId(), savedProductB.getId());
        productLikeRepository.save(savedUser2.getId(), savedProductB.getId());
        productLikeRepository.save(savedUser2.getId(), savedProductC.getId());

        LikeSummary likeSummary1 = LikeSummary.create(savedProductA.getId(), LikeTargetType.PRODUCT);
        likeSummary1.increase();
        LikeSummary likeSummary2 = LikeSummary.create(savedProductB.getId(), LikeTargetType.PRODUCT);
        likeSummary2.increase();
        likeSummary2.increase();
        likeSummaryRepository.save(likeSummary1);
        likeSummaryRepository.save(likeSummary2);
        LikeSummary likeSummary3 = LikeSummary.create(savedProductC.getId(), LikeTargetType.PRODUCT);
        likeSummary3.increase();
        likeSummary3.increase();
        likeSummary3.increase();
        likeSummaryRepository.save(likeSummary3);

        // when
        Pageable pageable = PageRequest.of(0, 10 , ProductSort.DENORMALIZED_LIKES_DESC.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsDenormalizedLikeCount(pageable , null);
        List<ProductInfo> content = productDetails.getContent();

        // then
        assertThat(content).hasSize(3)
                .extracting("productId", "productName", "price", "brandName", "likeCount")
                .containsExactly(
                        tuple(savedProductC.getId(), "상품C", 90000L, "nike", 3L),
                        tuple(savedProductB.getId(), "상품B", 70000L, "adidas", 2L),
                        tuple(savedProductA.getId(), "상품A", 50000L, "adidas", 1L)
                );

    }


    @Test
    @DisplayName("상품 목록 조회시 브랜드 정보와 좋아요 개수를 함께 조회한다. (비정규화 모델 + 최적화 쿼리)")
    @Transactional
    void findProductDetailsDenormalizedLikeCountOptimized() {
        // given
        User user1 = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User user2 = UserFixture.complete().set(Select.field(User::getUserId), "cony").create();
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);


        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "adidas").create();
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

        LikeSummary likeSummary1 = LikeSummary.create(savedProductA.getId(), LikeTargetType.PRODUCT);
        likeSummary1.increase();
        LikeSummary likeSummary2 = LikeSummary.create(savedProductB.getId(), LikeTargetType.PRODUCT);
        likeSummary2.increase();
        likeSummary2.increase();
        likeSummary2.increase();
        likeSummaryRepository.save(likeSummary1);
        likeSummaryRepository.save(likeSummary2);

        // when
        Pageable pageable = PageRequest.of(0, 10 , ProductSort.LIKES_DESC.toSort());
        Page<ProductInfo> productDetails = productRepository.findProductDetailsDenormalizedLikeCountOptimized(pageable , savedBrand.getId());
        List<ProductInfo> content = productDetails.getContent();

        // then
        assertThat(content).hasSize(2)
                .extracting("productId", "productName", "price", "brandName", "likeCount")
                .containsExactly(
                        tuple(savedProductB.getId(), "상품B", 70000L, "adidas", 3L),
                        tuple(savedProductA.getId(), "상품A", 50000L, "adidas", 1L)
                );
    }
}
