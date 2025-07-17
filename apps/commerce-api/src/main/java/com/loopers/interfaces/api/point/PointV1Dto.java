package com.loopers.interfaces.api.point;


public class PointV1Dto {
    public record PointResponse(
            String userId,
            Long value
    ) {
        public static PointResponse of(String userId, Long value) {
            return new PointResponse(userId, value);
        }

    }
    public record PointRequest(Long value) {

    }
}
