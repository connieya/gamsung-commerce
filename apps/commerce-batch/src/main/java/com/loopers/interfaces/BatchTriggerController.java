package com.loopers.interfaces;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor // curl -X POST 'http://localhost:8086/batch/weekly?asOfDate=2025-09-09'
@RequestMapping("/batch") // 임시 REST 엔드포인트로 트리거
public class BatchTriggerController {
    private final JobLauncher jobLauncher;
    private final Job buildWeeklyRankJob;
    private final Job buildMonthlyRankJob;

    @PostMapping("/weekly")
    public String run(@RequestParam String asOfDate) throws Exception {
        var params = new JobParametersBuilder()
                .addString("asOfDate", asOfDate)
                .toJobParameters();
        jobLauncher.run(buildWeeklyRankJob, params);
        return "OK";
    }

    @PostMapping("/monthly")
    public String runMonthly(@RequestParam String asOfDate) throws Exception {
        var params = new JobParametersBuilder()
                .addString("asOfDate", asOfDate)
                .toJobParameters();
        jobLauncher.run(buildMonthlyRankJob, params);
        return "Monthly Job Triggered OK";
    }
}
