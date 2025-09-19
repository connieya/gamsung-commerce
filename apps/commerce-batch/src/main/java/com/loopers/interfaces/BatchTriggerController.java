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
@RequiredArgsConstructor
@RequestMapping("/batch") // 임시 REST 엔드포인트로 트리거
public class BatchTriggerController {
    private final JobLauncher jobLauncher;
    private final Job buildWeeklyRankJob;

    @PostMapping("/weekly")
    public String run(@RequestParam String asOfDate) throws Exception {
        var params = new JobParametersBuilder()
                .addString("asOfDate", asOfDate)  // "2025-09-18"
                .toJobParameters();
        jobLauncher.run(buildWeeklyRankJob, params);
        return "OK";
    }
}
