package com.loopers.domain.like;

import com.loopers.domain.like.event.LikeEventPublisher;
import com.loopers.infrastructure.like.LikeSummaryGapLockJpaRepository;
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
class LikeServiceGapLockDeadlockTest {

    private final LikeServiceGapLock likeServiceGapLock;
    private final LikeSummaryGapLockJpaRepository likeSummaryGapLockJpaRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @MockitoBean
    private LikeEventPublisher likeEventPublisher;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("LikeSummary가 없는 상품에 10명이 동시에 좋아요하면 모두 성공해야 한다")
    void concurrent_first_likes_should_all_succeed() throws InterruptedException {
        // given — 정상적이라면 10명 모두 좋아요에 성공하고, likeCount는 10이어야 한다
        // LikeSummary 행이 아직 없는 상태에서 여러 사용자가 동시에 좋아요를 누른다
        int threadCount = 10;
        Long targetId = 9999L;
        LikeTargetType targetType = LikeTargetType.PRODUCT;

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
                    likeServiceGapLock.add(userId, targetId, targetType);
                } catch (Exception e) {
                    System.out.println("[Thread-" + userId + "] 예외 발생: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                    failCount.incrementAndGet();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        executor.shutdown();
        executor.awaitTermination(30, SECONDS);

        // then — 하지만 실제로는 실패한다
        // findByTargetForUpdate() → 행 없음 → InnoDB Gap Lock 획득
        // orElseGet(() -> save()) → 동시 INSERT 시도 → 순환 대기 → Deadlock (ERROR 1213)
        // 10개 중 9개 스레드가 CannotAcquireLockException으로 실패, 좋아요 1건만 기록된다
        long likeCount = likeSummaryGapLockJpaRepository
                .findByTarget(LikeTarget.create(targetId, targetType))
                .map(LikeSummary::getLikeCount)
                .orElse(0L);

        assertAll(
                () -> assertThat(failCount.get()).as("실패한 스레드 수").isEqualTo(0),
                () -> assertThat(likeCount).as("좋아요 수").isEqualTo(threadCount)
        );
    }
}
