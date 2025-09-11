package com.loopers.domain.rank;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.product.ProductCriteria;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.likes.ProductLikeService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

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
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductLikeService productLikeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private ProductFacade productFacade;

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

        Product product1 = ProductFixture.complete()
                .set(Select.field(Product::getName), "product1")
                .set(Select.field(Product::getPrice), 1000L)
                .create();


        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());

        // 상품 1 좋아요 & 조회
        productLikeService.add(savedUser.getId(), savedProduct1.getId());
        productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct1.getId()));

        //  상품 2 결제 완료 &  좋아요
        Product product2 = ProductFixture.complete()
                .set(Select.field(Product::getName), "product2")
                .set(Select.field(Product::getPrice), 2000L)
                .create();

        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());

        OrderCommand.OrderItem orderItem1 = OrderCommand.OrderItem.builder()
                .productId(savedProduct2.getId())
                .price(1000L)
                .quantity(1L)
                .build();

        OrderCommand orderCommand = OrderCommand.of(savedUser.getId(), List.of(orderItem1), 0L);
        Order initialOrder = Order.create(orderCommand);
        Order savedOrder = orderRepository.save(initialOrder);


        PaymentCriteria.Pay criteria = new PaymentCriteria.Pay(savedUser.getUserId(), savedOrder.getId(), PaymentMethod.POINT, CardType.HYUNDAI, "1234-1234-1234-1234", 1L);

        paymentFacade.pay(criteria);
        productLikeService.add(savedUser.getId(), savedProduct2.getId());


        Product product3 = ProductFixture.complete()
                .set(Select.field(Product::getName), "product3")
                .set(Select.field(Product::getPrice), 3000L)
                .create();

        Product savedProduct3 = productRepository.save(product3, savedBrand.getId());

        // 상품 3 조회
        productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct3.getId()));


        // 상품 4 결제 완료 & 조회
        Product product4 = ProductFixture.complete()
                .set(Select.field(Product::getName), "product4")
                .set(Select.field(Product::getPrice), 4000L)
                .create();

        Product savedProduct4 = productRepository.save(product4, savedBrand.getId());

        OrderCommand.OrderItem orderItem2 = OrderCommand.OrderItem.builder()
                .productId(savedProduct4.getId())
                .price(4000L)
                .quantity(1L)
                .build();

        OrderCommand orderCommand2 = OrderCommand.of(savedUser.getId(), List.of(orderItem2), 0L);
        Order initialOrder2 = Order.create(orderCommand2);
        Order savedOrder2 = orderRepository.save(initialOrder2);


        PaymentCriteria.Pay criteria2 = new PaymentCriteria.Pay(savedUser.getUserId(), savedOrder2.getId(), PaymentMethod.POINT, CardType.HYUNDAI, "1234-1234-1234-1234", 1L);

        paymentFacade.pay(criteria2);
        productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct4.getId()));

        // 상품 5 좋아요
        Product product5 = ProductFixture.complete()
                .set(Select.field(Product::getName), "product5")
                .set(Select.field(Product::getPrice), 5000L)
                .create();

        Product savedProduct5 = productRepository.save(product5, savedBrand.getId());

        productLikeService.add(savedUser.getId(), savedProduct5.getId());


        Thread.sleep(3000);

        // when
        RankingInfo firstPageProductRanking = rankingService.getProductRanking(LocalDate.now(), 1, 3);
        RankingInfo secondPageProductRanking = rankingService.getProductRanking(LocalDate.now(), 2, 3);


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

}
