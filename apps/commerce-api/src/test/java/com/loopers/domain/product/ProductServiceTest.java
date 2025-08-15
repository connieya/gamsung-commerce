package com.loopers.domain.product;

import com.loopers.domain.brand.BrandCacheRepository;
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
import static org.mockito.Mockito.*;

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

    @Mock
    BrandCacheRepository brandCacheRepository;

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
    @DisplayName("캐시에 브랜드 정보가 없을 때, DB를 조회하고 캐시에 저장한다.")
    void getProduct_with_cache_miss() {
        // given
        Long productId = 2L;
        Long brandId = 20L;
        Brand brandFromDb = Brand.create("아디다스","스포츠 브랜드");
        Product product = Product.create("울트라부스트", 150000L, brandId, ZonedDateTime.now());

        // 캐시 미스 상황을 Mocking: findById() 호출 시 empty를 반환
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(brandCacheRepository.findById(brandId)).thenReturn(Optional.empty());

        // DB 조회 시 brandFromDb를 반환하도록 Mocking
        when(brandRepository.findBrand(brandId)).thenReturn(Optional.of(brandFromDb));
        when(productLikeRepository.getLikeCount(productId)).thenReturn(20L);

        // when
        ProductDetailInfo productDetailInfo = productService.getProduct(productId);

        // then
        assertThat(productDetailInfo.getBrandName()).isEqualTo("아디다스");
        // 캐시 조회는 1번, DB 조회는 1번, 캐시 저장은 1번 호출되었는지 확인
        verify(brandCacheRepository, times(1)).findById(brandId);
        verify(brandRepository, times(1)).findBrand(brandId);
        verify(brandCacheRepository, times(1)).save(brandFromDb);
    }


    @Test
    @DisplayName("캐시에 브랜드 정보가 있을 때, DB를 조회하지 않고 캐시 데이터를 사용한다.")
    void getProduct_with_cache_hit() {
        // given
        Long productId = 1L;
        Long brandId = 10L;
        Brand cachedBrand = Brand.create("나이키", "스포츠 브랜드");
        Product product = Product.create("에어맥스", 100000L, brandId, ZonedDateTime.now());

        // 캐시 히트 상황을 Mocking: findById() 호출 시 캐시 데이터를 반환
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(brandCacheRepository.findById(brandId)).thenReturn(Optional.of(cachedBrand));
        when(productLikeRepository.getLikeCount(productId)).thenReturn(10L);

        // when
        ProductDetailInfo productDetailInfo = productService.getProduct(productId);

        // then
        // 1. 반환된 브랜드 이름이 캐시 데이터와 일치하는지 확인
        assertThat(productDetailInfo.getBrandName()).isEqualTo("나이키");

        // 2. brandCacheRepository의 findById()가 1번 호출되었는지 확인
        verify(brandCacheRepository, times(1)).findById(brandId);

        // 3. 캐시 히트이므로 DB 조회(brandRepository.findBrand)는 호출되지 않았는지 확인
        verify(brandRepository, never()).findBrand(any());

        // 4. 캐시 히트이므로 캐시 저장(brandCacheRepository.save)도 호출되지 않았는지 확인
        verify(brandCacheRepository, never()).save(any());
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
        when(productRepository.findProductDetails(PageRequest.of(0, 3, ProductSort.LIKES_DESC.toSort()))).thenReturn(targetData);
        ProductsInfo products = productService.getProducts_Old(3, 0, ProductSort.LIKES_DESC);
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
        when(productRepository.findProductDetails(PageRequest.of(0, 1, ProductSort.PRICE_ASC.toSort()))).thenReturn(targetData);
        ProductsInfo products = productService.getProducts_Old(1, 0, ProductSort.PRICE_ASC);
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
        when(productRepository.findProductDetails(PageRequest.of(0, 1, ProductSort.LATEST_DESC.toSort()))).thenReturn(targetData);
        ProductsInfo products = productService.getProducts_Old(1, 0, ProductSort.LATEST_DESC);
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
