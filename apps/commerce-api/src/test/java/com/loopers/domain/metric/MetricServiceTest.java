package com.loopers.domain.metric;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.Category;
import com.loopers.domain.likes.LikeSummary;
import com.loopers.domain.likes.LikeSummaryRepository;
import com.loopers.domain.likes.LikeTargetType;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.category.CategoryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MetricServiceTest {

    @Autowired
    LikeSummaryRepository likeSummaryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductService productService;

    @Autowired
    CategoryJpaRepository categoryJpaRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    void method() {
        // given
        User user = UserFixture.complete().create();
        User savedUser = userRepository.save(user);

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Category category = categoryJpaRepository.save(Category.createRoot("상의", 1));

        Product product = ProductFixture.create().brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct = productRepository.save(product);

        Product product2 = ProductFixture.create().brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct2 = productRepository.save(product2);

        Product product3 = ProductFixture.create().brand(savedBrand).categoryId(category.getId()).build();
        Product savedProduct3 = productRepository.save(product3);


        LikeSummary ls = LikeSummary.create(savedProduct3.getId(), LikeTargetType.PRODUCT);
        ls.increase();
        likeSummaryRepository.save(ls);

        productService.getProductDetail(savedProduct3.getId());
    }
}
