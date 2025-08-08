package com.loopers.domain.likes;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.fixture.BrandFixture;
import com.loopers.domain.product.fixture.ProductFixture;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.fixture.UserFixture;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ProductLikeServiceIntegrationTest {

    @Autowired
    ProductLikeService productLikeService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductLikeRepository productLikeRepository;

    @Test
    @DisplayName("동일한 상품에 대해 여러명이 좋아요/싫어요를 요청해도, 상품의 좋아요 개수가 정상 반영되어야 한다.")
    void add_increasesLikeCountCorrectly_whenCalledConcurrently() throws InterruptedException {
        // given
        final int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        List<User> users = IntStream.range(0, threadCount)
                .mapToObj(i -> UserFixture.complete().create())
                .map(user -> userRepository.save(user))
                .toList();

        Brand brand = BrandFixture.complete().create();
        Brand savedBrand = brandRepository.save(brand);

        Product product = ProductFixture.complete().set(Select.field(Product::getName), "foo1").create();

        Product savedProduct = productRepository.save(product, savedBrand.getId());

        // when
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드가 준비될 때까지 대기
                    productLikeService.add(user.getId(), savedProduct.getId());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown(); // 작업 완료를 알림
                }
            });
        }

        startLatch.countDown(); // 모든 스레드에게 시작 신호를 보냄
        endLatch.await();       // 모든 스레드가 작업을 마칠 때까지 대기

        executorService.shutdown(); // 스레드풀 종료


        // then
        Long likeCount = productLikeRepository.getLikeCount(savedProduct.getId());
        assertEquals(3, likeCount);

    }

}
