package com.loopers.domain.product;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    ProductLikeRepository productLikeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
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

            ProductCommand.Register productCommand = ProductCommand.Register.create("상품1", 100000L, savedBrand.getId());

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

                Product product1 = ProductFixture.complete()
                        .set(Select.field(Product::getName), "foo1")
                        .set(Select.field(Product::getPrice), 10000L)
                        .create();

                Product product2 = ProductFixture.complete()
                        .set(Select.field(Product::getName), "foo2")
                        .set(Select.field(Product::getPrice), 20000L)
                        .create();

                Product product3 = ProductFixture.complete()
                        .set(Select.field(Product::getName), "foo3")
                        .set(Select.field(Product::getPrice), 30000L)
                        .create();

                List.of(product1, product2, product3)
                        .forEach(product -> {
                            productRepository.save(product, savedBrand.getId());
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

                // 3. Product 영속화
                Product product1 = ProductFixture.complete()
                        .set(Select.field(Product::getName), "foo1")
                        .set(Select.field(Product::getPrice), 10000L)
                        .create();

                Product product2 = ProductFixture.complete()
                        .set(Select.field(Product::getName), "foo2")
                        .set(Select.field(Product::getPrice), 20000L)
                        .create();

                Product product3 = ProductFixture.complete()
                        .set(Select.field(Product::getName), "foo3")
                        .set(Select.field(Product::getPrice), 30000L)
                        .create();

                // Product를 영속화하고 저장된 객체를 필드에 저장
                Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
                Product savedProduct2 = productRepository.save(product2, savedBrand.getId());
                productRepository.save(product3, savedBrand.getId());

                // 4. 좋아요 데이터 추가
                // 위에서 저장한 User와 Product의 ID를 사용합니다.
                // product1에 대한 좋아요를 생성합니다.
                productLikeRepository.save(savedUser1.getId(), savedProduct1.getId());
                productLikeRepository.save(savedUser2.getId(), savedProduct1.getId());
                productLikeRepository.save(savedUser1.getId(), savedProduct2.getId());
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
