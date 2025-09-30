package com.loopers.config;

import com.loopers.job.MonthlyRankSnapshotTasklet;
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
public class MonthlyRankJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MonthlyRankSnapshotTasklet monthlyTasklet;

    @Bean
    public Step monthlyRankSnapshotStep() {
        return new StepBuilder("monthlyRankSnapshotStep", jobRepository)
                .tasklet(monthlyTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job buildMonthlyRankJob(Step monthlyRankSnapshotStep) {
        return new JobBuilder("buildMonthlyRankJob", jobRepository)
                .start(monthlyRankSnapshotStep)
                .build();
    }
}
