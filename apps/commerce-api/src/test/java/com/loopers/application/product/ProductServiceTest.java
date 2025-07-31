package com.loopers.application.product;

import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductDetailInfo;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.brand.Brand;
import com.loopers.domain.product.brand.BrandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ProductServiceTest {

    @InjectMocks
    ProductService productService;

    @Mock
    ProductRepository productRepository;

    @Mock
    BrandRepository brandRepository;

    @Mock
    ProductLikeRepository productLikeRepository;

    @Test
    @DisplayName("상품 상세 조회 시, 상품 정보, 브랜드 정보 및 좋아요 수가 성공적으로 함께 조회된다.")
    void getProductDetail() {
        // given
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2025, 8, 1, 10, 0, 0, 0, ZoneId.of("Asia/Seoul"));
        when(productRepository.findById(1L)).thenReturn(Optional.of(Product.create("상품1", 50000L, 1L, zonedDateTime)));
        when(brandRepository.findBrand(1L)).thenReturn(Optional.of(Brand.create("브랜드1", "멋진 브랜드")));
        when(productLikeRepository.getLikeCount(1L)).thenReturn(10L);

        // when
        ProductDetailInfo productDetailInfo = productService.getProduct(1L);

        // then
        assertAll(
                () -> assertThat(productDetailInfo.getProductName()).isEqualTo("상품1"),
                () -> assertThat(productDetailInfo.getProductPrice()).isEqualTo(50000L),
                () -> assertThat(productDetailInfo.getBrandName()).isEqualTo("브랜드1"),
                () -> assertThat(productDetailInfo.getLikeCount()).isEqualTo(10L)
        );
    }

}
