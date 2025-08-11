package com.loopers.domain.product;

import com.loopers.domain.common.Sort;
import com.loopers.domain.likes.ProductLikeRepository;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.Brand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.tuple;
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


    Page<ProductInfo> targetData;

    @BeforeEach
    void setUp() {
        List<ProductInfo> productInfos = new ArrayList<>();
        productInfos.add(new ProductInfo(1L, 10000L, "운동화", "나이키", 250L, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(10)));
        productInfos.add(new ProductInfo(2L, 25000L, "티셔츠", "아디다스", 100L, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(5)));
        productInfos.add(new ProductInfo(3L, 15000L, "바지", "퓨마", 20L, ZonedDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(15)));

        // Pageable 객체 생성: 현재 페이지 번호, 페이지 크기
        Pageable pageable = PageRequest.of(0, 1);

        // PageImpl 생성자를 사용하여 Page 객체 만들기
        // PageImpl(List<T> content, Pageable pageable, long total)
        targetData = new PageImpl<>(productInfos, pageable, 3);

    }


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

    @Test
    @DisplayName("상품 목록 가장 높은 좋아요 개수부터 정렬")
    void getProducts_withLikesDesc() {
        // given - BeforeEach

        // when
        when(productRepository.findProductDetails(PageRequest.of(0, 1))).thenReturn(targetData);
        ProductsInfo products = productService.getProducts(0, 1, Sort.LIKES_DESC);
        List<ProductInfo> productInfoList = products.getProductInfoList();

        // then
        assertThat(productInfoList).hasSize(3)
                .extracting("productId", "price", "productName", "brandName", "likeCount")
                .containsExactly(
                        tuple(1L, 10000L, "운동화", "나이키", 250L),
                        tuple(2L, 25000L, "티셔츠", "아디다스", 100L),
                        tuple(3L, 15000L, "바지", "퓨마", 20L)

                );
    }

    @Test
    @DisplayName("상품 목록 가장 낮은 가격부터 정렬")
    void getProducts_withPriceAscSort() {
        // given - BeforeEach

        // when
        when(productRepository.findProductDetails(PageRequest.of(0, 1))).thenReturn(targetData);
        ProductsInfo products = productService.getProducts(0, 1, Sort.PRICE_ASC);
        List<ProductInfo> productInfoList = products.getProductInfoList();

        // then
        assertThat(productInfoList).hasSize(3)
                .extracting("productId", "price", "productName", "brandName", "likeCount")
                .containsExactly(
                        tuple(1L, 10000L, "운동화", "나이키", 250L),
                        tuple(3L, 15000L, "바지", "퓨마", 20L),
                        tuple(2L, 25000L, "티셔츠", "아디다스", 100L)

                );
    }

    @Test
    @DisplayName("상품 목록 가장 최근 출시일 순으로 정렬")
    void getProducts_withLatestSort() {
        // given - BeforeEach

        // when
        when(productRepository.findProductDetails(PageRequest.of(0, 1))).thenReturn(targetData);
        ProductsInfo products = productService.getProducts(0, 1, Sort.LATEST);
        List<ProductInfo> productInfoList = products.getProductInfoList();

        // then
        assertThat(productInfoList).hasSize(3)
                .extracting("productId", "price", "productName", "brandName", "likeCount")
                .containsExactly(
                        tuple(2L, 25000L, "티셔츠", "아디다스", 100L),
                        tuple(1L, 10000L, "운동화", "나이키", 250L),
                        tuple(3L, 15000L, "바지", "퓨마", 20L)

                );
    }

}
