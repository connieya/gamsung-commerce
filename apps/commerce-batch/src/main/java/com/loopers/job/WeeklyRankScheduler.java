package com.loopers.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.ZoneId;

@Configuration
@RequiredArgsConstructor
public class WeeklyRankScheduler {

    private final JobLauncher jobLauncher;
    private final Job buildWeeklyRankJob;

    // 매일 00:05 KST
    @Scheduled(cron = "0 50 23 * * *", zone = "Asia/Seoul")
    public void runWeekly() throws Exception {
        LocalDate asOf = LocalDate.now(ZoneId.of("Asia/Seoul"));
        JobParameters params = new JobParametersBuilder()
                .addString("asOfDate", asOf.toString()) // 멱등/재시작 기준
                .toJobParameters();

        jobLauncher.run(buildWeeklyRankJob, params);
    }
}
