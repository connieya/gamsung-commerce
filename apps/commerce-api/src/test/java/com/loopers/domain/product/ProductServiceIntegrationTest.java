package com.loopers.domain.product;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTargetType;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.category.Category;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@SpringBootTest
class ProductServiceIntegrationTest {

    @Autowired
    ProductService productService;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    LikeSummaryRepository likeSummaryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisConnectionFactory.getConnection().serverCommands().flushDb();
    }


    @Nested
    @DisplayName("상품 등록")
    class Register {
        @Test
        @DisplayName("상품 등록에 성공한다.")
        void register() {
            // given
            Brand brand = BrandFixture.complete().create();
            Brand savedBrand = brandRepository.save(brand);

            categoryJpaRepository.save(Category.createRoot("상의", 1));

            ProductCommand.Register productCommand = ProductCommand.Register.create("상품1", 100000L, savedBrand.getId(), 1L);

            // when
            productService.register(productCommand);

            // then
            List<Product> products = productRepository.findByBrandId(savedBrand.getId());

            assertThat(products).hasSize(1)
                    .extracting("name", "price", "brandId")
                    .containsExactlyInAnyOrder(
                            tuple("상품1", 100000L, savedBrand.getId())
                    );

        }
    }


    @Nested
    @DisplayName("상품 상세 조회")
    class GetProduct {

        @Test
        @DisplayName("상품 상세 조회")
        void getProductDetail() {
            User user = UserFixture.complete()
                    .set(Select.field(User::getUserId), "gunny").create();
            User savedUser = userRepository.save(user);


            Brand brand = BrandFixture.complete()
                    .set(Select.field(Brand::getName), "nike").create();
            Brand savedBrand = brandRepository.save(brand);

            Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

            Product product = ProductFixture.create()
                    .name("foo1")
                    .price(10000L)
                    .brand(savedBrand)
                    .categoryId(category.getId())
                    .build();

            Product savedProduct = productRepository.save(product);

            LikeSummary likeSummary = LikeSummary.create(savedProduct.getId(), LikeTargetType.PRODUCT);
            likeSummary.increase();
            likeSummaryRepository.save(likeSummary);

            ProductDetailInfo productDetailInfo = productService.getProductDetail(savedProduct.getId());

            assertThat(productDetailInfo.getProductName()).isEqualTo("foo1");
            assertThat(productDetailInfo.getProductPrice()).isEqualTo(10000L);
            assertThat(productDetailInfo.getLikeCount()).isEqualTo(1L);
            assertThat(productDetailInfo.getBrandName()).isEqualTo("nike");
        }
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProducts {

        @Nested
        @DisplayName("가격 순 정렬")
        class SortByPrice {
            @BeforeEach
            void setupPriceData() {
                Brand brand = BrandFixture.complete()
                        .set(Select.field(Brand::getName), "nike").create();
                Brand savedBrand = brandRepository.save(brand);

                Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

                Product product1 = ProductFixture.create().name("foo1").price(10000L).brand(savedBrand).categoryId(category.getId()).build();
                Product product2 = ProductFixture.create().name("foo2").price(20000L).brand(savedBrand).categoryId(category.getId()).build();
                Product product3 = ProductFixture.create().name("foo3").price(30000L).brand(savedBrand).categoryId(category.getId()).build();

                List.of(product1, product2, product3)
                        .forEach(product -> {
                            productRepository.save(product);
                        });
            }

            @Test
            @DisplayName("상품 목록을 가격 낮은 순으로 정렬하여 조회한다.")
            void getProducts_returnsCorrectOrder_whenSortingByPriceAsc() {
                // given

                // when
                ProductsInfo products = productService.getProducts(3, 0, ProductSort.PRICE_ASC);
                List<ProductInfo> productInfoList = products.getProductInfoList();

                // then
                assertThat(productInfoList).hasSize(3)
                        .extracting("price", "productName", "brandName", "likeCount")
                        .containsExactly(
                                tuple(10000L, "foo1", "nike", 0L),
                                tuple(20000L, "foo2", "nike", 0L),
                                tuple(30000L, "foo3", "nike", 0L)
                        );
            }


            @Test
            @DisplayName("상품 목록을 가격 높은순으로 정렬하여 조회한다.")
            void getProducts_returnsCorrectOrder_whenSortingByPriceDesc() {
                // given

                // when
                ProductsInfo products = productService.getProducts(3, 0, ProductSort.PRICE_DESC);
                List<ProductInfo> productInfoList = products.getProductInfoList();

                // then
                assertThat(productInfoList).hasSize(3)
                        .extracting("price", "productName", "brandName", "likeCount")
                        .containsExactly(
                                tuple(30000L, "foo3", "nike", 0L),
                                tuple(20000L, "foo2", "nike", 0L),
                                tuple(10000L, "foo1", "nike", 0L)
                        );
            }
        }

        @Nested
        @DisplayName("좋아요 순 정렬")
        class SortByLikeCount {
            @BeforeEach
            void setupLikeCountData() {
                // 1. User 영속화
                User user1 = UserFixture.complete()
                        .set(Select.field(User::getUserId), "gunny").create();
                User savedUser1 = userRepository.save(user1);

                User user2 = UserFixture.complete()
                        .set(Select.field(User::getUserId), "cony").create();
                User savedUser2 = userRepository.save(user2);

                // 2. Brand 영속화
                Brand brand = BrandFixture.complete()
                        .set(Select.field(Brand::getName), "nike").create();
                Brand savedBrand = brandRepository.save(brand);

                Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

                // 3. Product 영속화
                Product product1 = ProductFixture.create().name("foo1").price(10000L).brand(savedBrand).categoryId(category.getId()).build();
                Product product2 = ProductFixture.create().name("foo2").price(20000L).brand(savedBrand).categoryId(category.getId()).build();
                Product product3 = ProductFixture.create().name("foo3").price(30000L).brand(savedBrand).categoryId(category.getId()).build();

                // Product를 영속화하고 저장된 객체를 필드에 저장
                Product savedProduct1 = productRepository.save(product1);
                Product savedProduct2 = productRepository.save(product2);
                productRepository.save(product3);

                // 4. LikeSummary 데이터 추가
                LikeSummary summary1 = LikeSummary.create(savedProduct1.getId(), LikeTargetType.PRODUCT);
                summary1.increase();
                summary1.increase();
                likeSummaryRepository.save(summary1);

                LikeSummary summary2 = LikeSummary.create(savedProduct2.getId(), LikeTargetType.PRODUCT);
                summary2.increase();
                likeSummaryRepository.save(summary2);
            }


            @Test
            @DisplayName("상품 목록을 좋아요 개수 낮은 순으로 정렬하여 조회한다.")
            void getProducts_returnsCorrectOrder_whenSortingByLikesAsc() {
                // given

                // when
                ProductsInfo products = productService.getProducts(3, 0, ProductSort.LIKES_ASC);
                List<ProductInfo> productInfoList = products.getProductInfoList();

                // then
                assertThat(productInfoList).hasSize(3)
                        .extracting("price", "productName", "brandName", "likeCount")
                        .containsExactly(
                                tuple(30000L, "foo3", "nike", 0L),
                                tuple(20000L, "foo2", "nike", 1L),
                                tuple(10000L, "foo1", "nike", 2L)
                        );
            }


            @Test
            @DisplayName("상품 목록을 좋아요 개수 높은 순으로 정렬하여 조회한다.")
            void getProducts_returnsCorrectOrder_whenSortingByLikesDesc() {
                // given

                // when
                ProductsInfo products = productService.getProducts(3, 0, ProductSort.LIKES_DESC);
                List<ProductInfo> productInfoList = products.getProductInfoList();

                // then
                assertThat(productInfoList).hasSize(3)
                        .extracting("price", "productName", "brandName", "likeCount")
                        .containsExactly(
                                tuple(10000L, "foo1", "nike", 2L),
                                tuple(20000L, "foo2", "nike", 1L),
                                tuple(30000L, "foo3", "nike", 0L)
                        );
            }

        }


    }


}
