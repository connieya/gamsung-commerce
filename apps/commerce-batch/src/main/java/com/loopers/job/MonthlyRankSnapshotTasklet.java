package com.loopers.job;

import com.loopers.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyRankSnapshotTasklet implements Tasklet {
    private final MetricRepository metricRepository;
    private final MvProductRankRepository mvProductRankRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        var params = chunkContext.getStepContext().getJobParameters();
        LocalDate asOf = LocalDate.parse((String) params.get("asOfDate"));
        LocalDate monthStart = asOf.with(TemporalAdjusters.firstDayOfMonth());

        BigDecimal wView  = RankingWeight.VIEW.getWeight();
        BigDecimal wLike  = RankingWeight.LIKE.getWeight();
        BigDecimal wOrder = RankingWeight.SALE.getWeight();

        List<MonthlyScoreRow> rows =
                metricRepository.findTop100ByMonthStartWithRank(monthStart, wLike, wOrder, wView);

        mvProductRankRepository.deleteByMonthStart(monthStart);

        int rank = 0;
        List<MvProductRankMonthly> insert = new ArrayList<>(rows.size());
        for (MonthlyScoreRow r : rows) {
            insert.add(MvProductRankMonthly.builder()
                    .monthStart(monthStart)
                    .productId(r.getProductId())
                    .likeSum(r.getLikeSum())
                    .orderSum(r.getOrderSum())
                    .viewSum(r.getViewSum())
                    .score(r.getScore())
                    .rank(++rank)
                    .build());
        }
        mvProductRankRepository.saveProductRankMonthlyAll(insert);

        contribution.incrementWriteCount(insert.size());
        return RepeatStatus.FINISHED;
    }
}
