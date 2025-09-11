package com.loopers.domain.metric;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.likes.ProductLikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MetricServiceTest {

    @Autowired
    ProductLikeService productLikeService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductService productService;

    @Test
    void method() {
        // given
        User user = UserFixture.complete().create();
        User savedUser = userRepository.save(user);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product = ProductFixture.complete().create();
        Product savedProduct = productRepository.save(product ,savedBrand.getId());

        Product product2 = ProductFixture.complete().create();
        Product savedProduct2 = productRepository.save(product2 ,savedBrand.getId());

        Product product3 = ProductFixture.complete().create();
        Product savedProduct3 = productRepository.save(product3 ,savedBrand.getId());


//        productLikeService.add(savedUser.getId(), savedProduct.getId());
//        productLikeService.add(savedUser.getId(), savedProduct2.getId());
        productLikeService.add(savedUser.getId(), savedProduct3.getId());

        productService.getProduct(savedProduct3.getId());
    }
}
