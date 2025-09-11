package com.loopers.domain.likes;

import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LikeSummaryRepositoryTest {


    @Autowired
    private LikeSummaryRepository likeSummaryRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @Test
    @DisplayName("좋아요 집계 객체 저장 후 조회 시, 좋아요 수가 정확해야 한다.")
    void saveAndRetrieve_verifiesLikeCountIsCorrect() {
        // given
        LikeSummary likeSummary = LikeSummary.create(1L, LikeTargetType.PRODUCT);
        likeSummary.increase();

        likeSummaryRepository.save(likeSummary);

        // when
        LikeSummary updatedLikeSummary = likeSummaryRepository.findByTarget(LikeTarget.create(1L, LikeTargetType.PRODUCT)).get();

        // then
        assertThat(updatedLikeSummary.getLikeCount()).isEqualTo(1L);
    }

    @Test
    @Transactional
    void updateLikeCountBy() {
        // given
        LikeSummary likeSummary = LikeSummary.create(1L, LikeTargetType.PRODUCT);
        likeSummary.increase();

        likeSummaryRepository.save(likeSummary);
        likeSummaryRepository.updateLikeCountBy(1L, LikeTargetType.PRODUCT, 2L);
        // when
        LikeSummary updatedLikeSummary = likeSummaryRepository.findByTarget(LikeTarget.create(1L, LikeTargetType.PRODUCT)).get();

        // then
        assertThat(updatedLikeSummary.getLikeCount()).isEqualTo(3L);
    }

}
