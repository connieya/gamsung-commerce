package com.loopers.job;

import com.loopers.domain.MetricRepository;
import com.loopers.domain.MvProductRankWeekly;
import com.loopers.domain.MvProductRankRepository;
import com.loopers.domain.WeeklyScoreRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WeeklyRankSnapShotTasklet implements Tasklet {
    private final MetricRepository metricRepository;
    private final MvProductRankRepository mvProductRankRepository;

    @Value("${rank.weight.like:1.0}")  private BigDecimal wLike;
    @Value("${rank.weight.order:5.0}") private BigDecimal wOrder;
    @Value("${rank.weight.view:0.1}")  private BigDecimal wView;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        var params = chunkContext.getStepContext().getJobParameters();
        LocalDate asOf = LocalDate.parse((String) params.get("asOfDate"));
        LocalDate weekStart = asOf.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 1) DB에서 점수/랭크까지 계산해 TOP100 가져오기
        List<WeeklyScoreRow> rows =
                metricRepository.findTop100ByWeekStartWithRank(weekStart, wLike, wOrder, wView);

        // 2) 기존 주차 MV 삭제
        mvProductRankRepository.deleteByWeekStart(weekStart);

        // 3) INSERT (TOP100, 랭크는 DB에서 계산된 rnk 사용)
        int rank = 0;
        List<MvProductRankWeekly> mvProductRankWeeklies = new ArrayList<>(rows.size());
        for (WeeklyScoreRow r : rows) {
            mvProductRankWeeklies.add(MvProductRankWeekly.builder()
                    .weekStart(weekStart)
                    .productId(r.getProductId())
                    .likeSum(r.getLikeSum())
                    .orderSum(r.getOrderSum())
                    .viewSum(r.getViewSum())
                    .score(r.getScore())
                    .rank(++rank)
                    .build());
        }
        mvProductRankRepository.saveAll(mvProductRankWeeklies);

        contribution.incrementWriteCount(mvProductRankWeeklies.size());
        return RepeatStatus.FINISHED;
    }
}
