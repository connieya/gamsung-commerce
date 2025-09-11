package com.loopers.interfaces.api.rank;

import com.loopers.domain.rank.RankingInfo;
import com.loopers.domain.rank.RankingService;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
public class RankingV1Controller {

    private final RankingService rankingService;

    @GetMapping
    public ApiResponse<RankingV1Dto.SummaryResponse> getRanking(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam LocalDate date
    ) {

        RankingInfo rankingInfo = rankingService.getProductRanking(date, page, size);
        return ApiResponse.success(RankingV1Dto.SummaryResponse.from(rankingInfo));
    }
}
