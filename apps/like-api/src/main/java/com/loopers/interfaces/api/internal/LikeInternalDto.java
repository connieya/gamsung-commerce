package com.loopers.interfaces.api.internal;

import com.loopers.domain.like.LikeTargetType;

import java.util.List;
import java.util.Map;

public class LikeInternalDto {

    public record LikeCountRequest(List<LikeCountItem> items) {
        public record LikeCountItem(Long targetId, LikeTargetType targetType) {}
    }

    public record LikeCountResponse(Map<Long, Long> counts) {}

    public record UserLikeIdsResponse(List<Long> targetIds) {}
}
