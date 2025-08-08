package com.loopers.application.order;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.coupon.*;
import com.loopers.domain.coupon.exception.CouponException;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderFacadeIntegrationTest {

    @Autowired
    OrderFacade orderFacade;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    UserCouponRepository userCouponRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void cleanUp() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("사용 불가능하거나 존재하지 않는 쿠폰일 경우 주문은 실패해야 한다.")
    void placeOrder_throwsException_whenUserCouponIsInvalid() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        userRepository.save(user);

        Product product1 = ProductFixture.complete().set(Select.field(Product::getName), "foo1").create();
        Product product2 = ProductFixture.complete().set(Select.field(Product::getName), "foo2").create();

        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);


        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());


        OrderCriteria.OrderItem orderItem1 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct1.getId())
                .quantity(3L)
                .build();

        OrderCriteria.OrderItem orderItem2 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct2.getId())
                .quantity(2L)
                .build();

        Coupon savedCoupon = couponRepository.save(Coupon.create("이벤트", CouponType.PERCENTAGE, 10L));

        OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem1, orderItem2), savedCoupon.getId(), UUID.randomUUID().toString());


        // when & then
        assertThatThrownBy(() -> {
            orderFacade.place(orderCriteria);
        }).isInstanceOf(CouponException.UserCouponNotFoundException.class);
    }

    @Test
    @DisplayName("주문 성공: 쿠폰 할인이 정상 적용되어 총액 및 할인 금액이 올바르게 계산된다.")
    void placeOrder_success_withCouponApplied() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        User savedUser = userRepository.save(user);

        Product product1 = ProductFixture.complete()
                .set(Select.field(Product::getName), "foo1")
                .set(Select.field(Product::getPrice), 3000L)
                .create();
        Product product2 = ProductFixture.complete()
                .set(Select.field(Product::getName), "foo2")
                .set(Select.field(Product::getPrice), 2000L)
                .create();

        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);


        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());


        OrderCriteria.OrderItem orderItem1 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct1.getId())
                .quantity(5L)
                .build();

        OrderCriteria.OrderItem orderItem2 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct2.getId())
                .quantity(1L)
                .build();

        Coupon savedCoupon = couponRepository.save(Coupon.create("이벤트", CouponType.PERCENTAGE, 10L));
        userCouponRepository.save(UserCoupon.create(savedUser.getId(), savedCoupon.getId()));

        OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem1, orderItem2), savedCoupon.getId(), UUID.randomUUID().toString());


        // when
        OrderResult.Create orderResult = orderFacade.place(orderCriteria);


        // then
        assertAll(
                () -> assertThat(orderResult.getTotalAmount()).isEqualTo(17000L),
                () -> assertThat(orderResult.getDiscountAmount()).isEqualTo(1700L)

        );
    }

    @Test
    @DisplayName("주문 성공: 쿠폰 미사용 시, 할인 금액이 0원으로 올바르게 계산된다.")
    void placeOrder_success_withoutCouponApplied() {
        // given
        User user = UserFixture.complete().set(Select.field(User::getUserId), "gunny").create();
        userRepository.save(user);

        Product product1 = ProductFixture.complete()
                .set(Select.field(Product::getName), "foo1")
                .set(Select.field(Product::getPrice), 3000L)
                .create();
        Product product2 = ProductFixture.complete()
                .set(Select.field(Product::getName), "foo2")
                .set(Select.field(Product::getPrice), 2000L)
                .create();

        Brand brand = BrandFixture.complete().set(Select.field(Brand::getName), "nike").create();
        Brand savedBrand = brandRepository.save(brand);


        Product savedProduct1 = productRepository.save(product1, savedBrand.getId());
        Product savedProduct2 = productRepository.save(product2, savedBrand.getId());


        OrderCriteria.OrderItem orderItem1 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct1.getId())
                .quantity(5L)
                .build();

        OrderCriteria.OrderItem orderItem2 = OrderCriteria.OrderItem
                .builder()
                .productId(savedProduct2.getId())
                .quantity(1L)
                .build();


        OrderCriteria orderCriteria = new OrderCriteria("gunny", List.of(orderItem1, orderItem2), 0L, UUID.randomUUID().toString());


        // when
        OrderResult.Create orderResult = orderFacade.place(orderCriteria);


        // then
        assertAll(
                () -> assertThat(orderResult.getTotalAmount()).isEqualTo(17000L),
                () -> assertThat(orderResult.getDiscountAmount()).isEqualTo(0L)

        );
    }


}
