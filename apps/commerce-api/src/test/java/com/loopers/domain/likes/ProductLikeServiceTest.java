package com.loopers.domain.likes;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.fixture.UserFixture;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.likes.LikeSummaryJpaRepository;
import com.loopers.infrastructure.likes.ProductLikeJpaRepository;
import com.loopers.infrastructure.product.ProductEntity;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserEntity;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;


import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@SpringBootTest
class ProductLikeServiceTest {

    @Autowired
    ProductLikeService productLikeService;

    @MockitoSpyBean
    ProductLikeJpaRepository productLikeJpaRepository;

    @MockitoSpyBean
    LikeSummaryJpaRepository likeSummaryJpaRepository;

    @MockitoSpyBean
    UserJpaRepository userJpaRepository;

    @MockitoSpyBean
    ProductJpaRepository productJpaRepository;

    @MockitoSpyBean
    BrandJpaRepository brandJpaRepository;

    @Autowired
    DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @Test
    @DisplayName("좋아요: 요청 시 성공적으로 등록된다.")
    void add_successfullyRegistersLike() {
        // given
        User user = UserFixture.complete().create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        UserEntity savedUser = userJpaRepository.save(userEntity);

        Product product = ProductFixture.complete().create();
        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandJpaRepository.save(brand);
        ProductEntity productEntity = ProductEntity.fromDomain(product, savedBrand);
        ProductEntity savedProduct = productJpaRepository.save(productEntity);

        // when
        doReturn(Optional.of(userEntity))
                .when(userJpaRepository)
                .findById(savedUser.getId());

        doReturn(Optional.of(productEntity))
                .when(productJpaRepository)
                .findById(savedProduct.getId());

        doReturn(false)
                .when(productLikeJpaRepository)
                .existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId());

        productLikeService.add(savedUser.getId(), savedProduct.getId());

        // then

        verify(productLikeJpaRepository, times(1)).save(any(ProductLike.class));

        verify(likeSummaryJpaRepository, times(1)).save(any(LikeSummary.class));
    }


    @Test
    @DisplayName("좋아요: 이미 등록된 경우, 중복 저장되지 않아 멱등성이 보장된다.")
    void add_ensuresIdempotency_onDuplicate() {
        // given
        User user = UserFixture.complete().create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        UserEntity savedUser = userJpaRepository.save(userEntity);

        Product product = ProductFixture.complete().create();
        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandJpaRepository.save(brand);
        ProductEntity productEntity = ProductEntity.fromDomain(product, savedBrand);
        ProductEntity savedProduct = productJpaRepository.save(productEntity);

        // when
        doReturn(Optional.of(userEntity))
                .when(userJpaRepository)
                .findById(savedUser.getId());

        doReturn(Optional.of(productEntity))
                .when(productJpaRepository)
                .findById(savedProduct.getId());

        doReturn(true)
                .when(productLikeJpaRepository)
                .existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId());

        productLikeService.add(savedUser.getId(), savedProduct.getId());

        // then
        verify(productLikeJpaRepository, never()).save(any(ProductLike.class));
    }

    @Test
    @DisplayName("좋아요 취소 요청 시 성공적으로 삭제된다.")
    void remove_successfullyDeletesLike() {
        // given
        User user = UserFixture.complete().create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        UserEntity savedUser = userJpaRepository.save(userEntity);

        Product product = ProductFixture.complete().create();
        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandJpaRepository.save(brand);
        ProductEntity productEntity = ProductEntity.fromDomain(product, savedBrand);
        ProductEntity savedProduct = productJpaRepository.save(productEntity);

        // when
        doReturn(Optional.of(userEntity))
                .when(userJpaRepository)
                .findById(savedUser.getId());

        doReturn(Optional.of(productEntity))
                .when(productJpaRepository)
                .findById(savedProduct.getId());

        doReturn(true)
                .when(productLikeJpaRepository)
                .existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId());

        LikeSummary likeSummary = LikeSummary.create(savedProduct.getId(), LikeTargetType.PRODUCT);
        likeSummary.increase();
        LikeSummary savedLikeSummary = likeSummaryJpaRepository.save(likeSummary);
        LikeTarget target = savedLikeSummary.getTarget();

        doReturn(Optional.of(likeSummary))
                .when(likeSummaryJpaRepository)
                .findByTargetForUpdate(target);

        productLikeService.remove(savedUser.getId(), savedProduct.getId());

        // then
        verify(productLikeJpaRepository, times(1)).deleteByUserIdAndProductId(savedUser.getId(), savedProduct.getId());
        verify(likeSummaryJpaRepository, times(1)).findByTargetForUpdate(target);
    }


    @Test
    @DisplayName("좋아요 등록 시 집계 반영에 실패해도, 좋아요 등록은 성공한다.")
    void addLike_whenSummaryUpdateFails_addsLikeSuccessfully() {
        // given
        User user = UserFixture.complete().create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        UserEntity savedUser = userJpaRepository.save(userEntity);

        Product product = ProductFixture.complete().create();
        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandJpaRepository.save(brand);
        ProductEntity productEntity = ProductEntity.fromDomain(product, savedBrand);
        ProductEntity savedProduct = productJpaRepository.save(productEntity);

        // when
        doThrow(new RuntimeException("boom"))
                .when(likeSummaryJpaRepository).findByTargetForUpdate(any());

        productLikeService.add(savedUser.getId(), savedProduct.getId());

        boolean existsByUserIdAndProductId = productLikeJpaRepository.existsByUserIdAndProductId(savedUser.getId(), savedProduct.getId());
        Optional<LikeSummary> likeSummary = likeSummaryJpaRepository.findByTarget(LikeTarget.create(savedProduct.getId(), LikeTargetType.PRODUCT));

        // then
        assertThat(existsByUserIdAndProductId).isEqualTo(true);
        assertThat(likeSummary.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("좋아요 취소 시 집계 반영에 실패해도, 좋아요 취소는 성공한다.")
    void removeLike_whenSummaryUpdateFails_removesLikeSuccessfully() {
        // given
        User user1 = UserFixture.complete().create();
        UserEntity userEntity1 = UserEntity.fromDomain(user1);
        UserEntity savedUser1 = userJpaRepository.save(userEntity1);

        Product product = ProductFixture.complete().create();
        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandJpaRepository.save(brand);
        ProductEntity productEntity = ProductEntity.fromDomain(product, savedBrand);
        ProductEntity savedProduct = productJpaRepository.save(productEntity);

        // when
        productLikeJpaRepository.save(ProductLike.create(savedUser1.getId(), savedProduct.getId()));

        productLikeService.remove(savedUser1.getId(), savedProduct.getId());

        List<ProductLike> productLike = productLikeJpaRepository.findByProductId(savedProduct.getId());
        Optional<LikeSummary> likeSummary = likeSummaryJpaRepository.findByTarget(LikeTarget.create(savedProduct.getId(), LikeTargetType.PRODUCT));

        // then
        assertThat(productLike.size()).isEqualTo(0L);
        assertThat(likeSummary.isEmpty()).isTrue();
    }


}
