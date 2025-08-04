package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Test
    @DisplayName("상품 등록에 성공한다.")
    void register() {
        // given
        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        ProductCommand productCommand = ProductCommand.of("상품1", 100000L, savedBrand.getId());


        // when
        productService.register(productCommand);

        // then
        List<Product> products = productRepository.findByBrandId(savedBrand.getId());

        assertThat(products).hasSize(1)
                .extracting("name" ,"price" ,"brandId")
                .containsExactlyInAnyOrder(
                        tuple("상품1", 100000L, savedBrand.getId())
                );

    }


}
