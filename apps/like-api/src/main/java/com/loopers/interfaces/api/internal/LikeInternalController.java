package com.loopers.interfaces.api.internal;

import com.loopers.domain.like.*;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/v1/likes")
@RequiredArgsConstructor
public class LikeInternalController {

    private final LikeService likeService;
    private final LikeSummaryRepository likeSummaryRepository;

    @PostMapping("/counts")
    public ApiResponse<LikeInternalDto.LikeCountResponse> getLikeCounts(
            @RequestBody LikeInternalDto.LikeCountRequest request) {
        List<LikeTarget> targets = request.items().stream()
                .map(item -> LikeTarget.create(item.targetId(), item.targetType()))
                .toList();

        List<LikeSummary> summaries = likeSummaryRepository.findByTargets(targets);
        Map<Long, Long> counts = summaries.stream()
                .collect(Collectors.toMap(
                        s -> s.getTarget().getId(),
                        LikeSummary::getLikeCount
                ));

        return ApiResponse.success(new LikeInternalDto.LikeCountResponse(counts));
    }

    @GetMapping("/{targetType}/users/{userId}")
    public ApiResponse<LikeInternalDto.UserLikeIdsResponse> getUserLikeIds(
            @PathVariable("targetType") LikeTargetType targetType,
            @PathVariable("userId") Long userId) {
        List<Long> targetIds = likeService.findTargetIdsByUserIdAndTargetType(userId, targetType);
        return ApiResponse.success(new LikeInternalDto.UserLikeIdsResponse(targetIds));
    }
}
