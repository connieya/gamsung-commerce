package com.loopers.domain.like;

import com.loopers.domain.like.event.LikeEventPublisher;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class LikeServiceConcurrencyTest {

    private final LikeService likeService;
    private final LikeSummaryRepository likeSummaryRepository;
    private final LikeRepository likeRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @MockitoBean
    private LikeEventPublisher likeEventPublisher;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("여러 사용자가 동시에 같은 상품에 좋아요하면 count가 정확해야 한다")
    void concurrent_add_should_maintain_correct_count() throws InterruptedException {
        // given
        int threadCount = 100;
        Long targetId = 1L;
        LikeTargetType targetType = LikeTargetType.PRODUCT;

        likeSummaryRepository.save(LikeSummary.create(targetId, targetType));

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            long userId = i + 1;
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    likeService.add(userId, targetId, targetType);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(30, SECONDS);

        // then
        LikeSummary summary = likeSummaryRepository
                .findByTarget(LikeTarget.create(targetId, targetType))
                .orElseThrow();

        assertAll(
                () -> assertThat(summary.getLikeCount()).isEqualTo(100L),
                () -> assertThat(failCount.get()).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("동시에 좋아요 추가와 삭제가 혼합되면 count가 정확해야 한다")
    void concurrent_add_and_remove_should_maintain_correct_count() throws InterruptedException {
        // given
        int addCount = 50;
        int removeCount = 50;
        int totalThreads = addCount + removeCount;
        Long targetId = 1L;
        LikeTargetType targetType = LikeTargetType.PRODUCT;

        // 초기 상태: 50명이 이미 좋아요 (userId 1~50)
        likeSummaryRepository.save(LikeSummary.create(targetId, targetType));
        for (long userId = 1; userId <= removeCount; userId++) {
            likeService.add(userId, targetId, targetType);
        }

        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch readyLatch = new CountDownLatch(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 50명 추가(userId 51~100) + 기존 50명 삭제(userId 1~50)
        for (int i = 0; i < addCount; i++) {
            long userId = removeCount + i + 1; // 51~100
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    likeService.add(userId, targetId, targetType);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        for (int i = 0; i < removeCount; i++) {
            long userId = i + 1; // 1~50
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();
                    likeService.remove(userId, targetId, targetType);
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(30, SECONDS);

        // then
        LikeSummary summary = likeSummaryRepository
                .findByTarget(LikeTarget.create(targetId, targetType))
                .orElseThrow();

        assertAll(
                () -> assertThat(summary.getLikeCount()).isEqualTo(50L),
                () -> assertThat(failCount.get()).isEqualTo(0)
        );
    }
}
