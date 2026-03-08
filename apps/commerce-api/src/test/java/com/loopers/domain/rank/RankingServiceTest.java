package com.loopers.domain.rank;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.Category;
import com.loopers.domain.likes.ProductLikeService;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PayKind;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.infrastructure.feign.order.OrderApiClient;
import com.loopers.infrastructure.feign.order.OrderApiDto;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
class RankingServiceTest {

    @Autowired
    private RankingService rankingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductLikeService productLikeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private ProductFacade productFacade;

    @MockitoBean
    private OrderApiClient orderApiClient;

    @AfterEach
    void cleanUp() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("오늘의 인기 상품을 조회한다.")
    void getProductRanking() throws InterruptedException {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "pgh").create();
        User savedUser = userRepository.save(user);

        pointRepository.save(Point.create(savedUser.getUserId(), 1000_0000L));

        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product1 = ProductFixture.create().name("product1").price(1000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct1 = productRepository.save(product1);

        productLikeService.add(savedUser.getId(), savedProduct1.getId());
        productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct1.getId()));

        Product product2 = ProductFixture.create().name("product2").price(2000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct2 = productRepository.save(product2);

        // Mock order for product2 payment
        OrderApiDto.OrderResponse orderResp1 = new OrderApiDto.OrderResponse(
                100L, "ORD-RANK-001", 1000L, 0L, 1000L, "INIT", savedUser.getId(),
                List.of(new OrderApiDto.OrderLineResponse(savedProduct2.getId(), 1L, 1000L))
        );
        when(orderApiClient.getOrder(100L)).thenReturn(ApiResponse.success(orderResp1));

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(savedUser.getUserId(), 100L, PaymentMethod.POINT, PayKind.POINT, CardType.HYUNDAI, "1234-1234-1234-1234", 1L);
        paymentFacade.pay(criteria);
        productLikeService.add(savedUser.getId(), savedProduct2.getId());

        Product product3 = ProductFixture.create().name("product3").price(3000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct3 = productRepository.save(product3);
        productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct3.getId()));

        Product product4 = ProductFixture.create().name("product4").price(4000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct4 = productRepository.save(product4);

        // Mock order for product4 payment
        OrderApiDto.OrderResponse orderResp2 = new OrderApiDto.OrderResponse(
                200L, "ORD-RANK-002", 4000L, 0L, 4000L, "INIT", savedUser.getId(),
                List.of(new OrderApiDto.OrderLineResponse(savedProduct4.getId(), 1L, 4000L))
        );
        when(orderApiClient.getOrder(200L)).thenReturn(ApiResponse.success(orderResp2));

        PaymentCriteria.Pay criteria2 = new PaymentCriteria.Pay(savedUser.getUserId(), 200L, PaymentMethod.POINT, PayKind.POINT, CardType.HYUNDAI, "1234-1234-1234-1234", 1L);
        paymentFacade.pay(criteria2);
        productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct4.getId()));

        Product product5 = ProductFixture.create().name("product5").price(5000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct5 = productRepository.save(product5);
        productLikeService.add(savedUser.getId(), savedProduct5.getId());

        Thread.sleep(3000);

        // when
        RankingInfo firstPageProductRanking = rankingService.getProductRanking(RankingCommand.GetProducts.of(LocalDate.now(), 1, 3));
        RankingInfo secondPageProductRanking = rankingService.getProductRanking(RankingCommand.GetProducts.of(LocalDate.now(), 2, 3));

        // then
        assertAll(
                () -> assertThat(firstPageProductRanking.getProductInfos()).hasSize(3)
                        .extracting("productName", "price", "likeCount")
                        .containsExactly(
                                tuple("product2", 2000L, 1L),
                                tuple("product4", 4000L, 0L),
                                tuple("product1", 1000L, 1L)
                        ),
                () -> assertThat(secondPageProductRanking.getProductInfos()).hasSize(2)
                        .extracting("productName", "price", "likeCount")
                        .containsExactly(
                                tuple("product5", 5000L, 1L),
                                tuple("product3", 3000L, 0L)
                        )
        );
    }

    @Test
    @DisplayName("해당 상품의 랭킹 순위를 조회한다.")
    void getRankOfProduct() throws InterruptedException {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "pgh").create();
        User savedUser = userRepository.save(user);

        pointRepository.save(Point.create(savedUser.getUserId(), 1000_0000L));

        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product1 = ProductFixture.create().name("product1").price(1000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct1 = productRepository.save(product1);

        productLikeService.add(savedUser.getId(), savedProduct1.getId());
        productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct1.getId()));

        Product product2 = ProductFixture.create().name("product2").price(2000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct2 = productRepository.save(product2);

        OrderApiDto.OrderResponse orderResp = new OrderApiDto.OrderResponse(
                100L, "ORD-RANK-003", 1000L, 0L, 1000L, "INIT", savedUser.getId(),
                List.of(new OrderApiDto.OrderLineResponse(savedProduct2.getId(), 1L, 1000L))
        );
        when(orderApiClient.getOrder(100L)).thenReturn(ApiResponse.success(orderResp));

        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(savedUser.getUserId(), 100L, PaymentMethod.POINT, PayKind.POINT, CardType.HYUNDAI, "1234-1234-1234-1234", 1L);
        paymentFacade.pay(criteria);
        productLikeService.add(savedUser.getId(), savedProduct2.getId());

        Product product3 = ProductFixture.create().name("product3").price(3000L).brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct3 = productRepository.save(product3);
        productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct3.getId()));

        Thread.sleep(2000);

        // when
        Long rankOfProduct = rankingService.getRankOfProduct(RankingCommand.GetProduct.of(LocalDate.now(), savedProduct1.getId()));

        // then
        assertAll(
                () -> assertThat(rankOfProduct).isNotNull(),
                () -> assertThat(rankOfProduct).isEqualTo(2L)
        );
    }
}
