package com.loopers.domain.likes;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LikeSummaryServiceTest {

    @Autowired
    private LikeSummaryService likeSummaryService;

    @Autowired
    private LikeSummaryRepository likeSummaryRepository;

    @Test
    void update() {
        // given
        LikeCommand.Update.Item item = LikeCommand.Update.Item.of("event", 1L, LikeUpdateType.DECREMENT);
        List<LikeCommand.Update.Item> items = new ArrayList<>();
        items.add(item);

        LikeSummary likeSummary = LikeSummary.create(1L, LikeTargetType.PRODUCT);
        likeSummary.increase();
        likeSummary.increase();
        likeSummaryRepository.save(likeSummary);

        // when
        likeSummaryService.update(new LikeCommand.Update(items));

        // then
        LikeSummary updateLikeSummary = likeSummaryRepository.findByTarget(LikeTarget.create(1L, LikeTargetType.PRODUCT)).get();
        assertThat(updateLikeSummary.getLikeCount()).isEqualTo(1L);
    }

}
