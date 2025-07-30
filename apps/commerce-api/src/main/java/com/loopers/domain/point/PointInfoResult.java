package com.loopers.domain.point;


public record PointInfoResult(String userId, Long value) {
    public static PointInfoResult of(String userId, Long value) {
        return new PointInfoResult(userId, value);
    }

}
