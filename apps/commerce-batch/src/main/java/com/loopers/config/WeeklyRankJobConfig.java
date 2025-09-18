package com.loopers.config;

import com.loopers.job.WeeklyRankSnapShotTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class WeeklyRankJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WeeklyRankSnapShotTasklet weeklyTasklet;

    @Bean
    public Step weeklyRankSnapshotStep() {
        return new StepBuilder("weeklyRankSnapshotStep", jobRepository)
                .tasklet(weeklyTasklet, transactionManager) // 한 트랜잭션에서 삭제→삽입
                .build();
    }

    @Bean
    public Job buildWeeklyRankJob(Step weeklyRankSnapshotStep) {
        return new JobBuilder("buildWeeklyRankJob", jobRepository)
                .start(weeklyRankSnapshotStep)
                .build();
    }
}
