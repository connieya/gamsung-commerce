package com.loopers.application.product;

import com.loopers.application.payment.PaymentCriteria;
import com.loopers.application.payment.PaymentFacade;
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
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductFacadeTest {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductLikeService productLikeService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentFacade paymentFacade;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @Autowired
    private PointRepository pointRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }


    @Test
    @DisplayName("상품 상세 조회 시, 좋아요 개수와 랭킹 순위가 포함된 정보가 정확히 포함되어야 한다.")
    void getProductDetail() throws InterruptedException {
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

        // 상품 1 좋아요
        productLikeService.add(savedUser.getId(), savedProduct1.getId());

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




        // when
        ProductResult productDetail = productFacade.getProductDetail(new ProductCriteria.GetDetail(savedProduct1.getId()));

        Thread.sleep(3000);
        // then
        assertThat(productDetail.getProductName()).isEqualTo("product1");
        assertThat(productDetail.getProductPrice()).isEqualTo(1000L);
        assertThat(productDetail.getLikeCount()).isEqualTo(1L);
        assertThat(productDetail.getRank()).isEqualTo(2L);
    }

}
