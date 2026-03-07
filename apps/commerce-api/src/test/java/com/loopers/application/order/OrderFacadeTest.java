package com.loopers.application.order;

import com.loopers.domain.cart.CartService;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderNoIssuer;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @InjectMocks
    OrderFacade orderFacade;

    @Mock
    UserService userService;

    @Mock
    ProductService productService;

    @Mock
    OrderService orderService;

    @Mock
    OrderNoIssuer orderNoIssuer;

    @Mock
    CartService cartService;

    @Mock
    PaymentService paymentService;

    @Mock
    CouponService couponService;
}
