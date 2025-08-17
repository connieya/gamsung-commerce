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
        ProductEntity productEntity = ProductEntity.fromDomain(product,savedBrand);
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
        ProductEntity productEntity = ProductEntity.fromDomain(product,savedBrand);
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
    @DisplayName("좋아요: 요청 시 성공적으로 삭제된다.")
    void remove_successfullyDeletesLike() {
        // given
        User user = UserFixture.complete().create();
        UserEntity userEntity = UserEntity.fromDomain(user);
        UserEntity savedUser = userJpaRepository.save(userEntity);

        Product product = ProductFixture.complete().create();
        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandJpaRepository.save(brand);
        ProductEntity productEntity = ProductEntity.fromDomain(product,savedBrand);
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

}
