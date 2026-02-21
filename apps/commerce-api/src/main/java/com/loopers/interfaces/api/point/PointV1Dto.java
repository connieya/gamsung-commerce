package com.loopers.interfaces.api.point;


public class PointV1Dto {
    public static class Request {
        public record Charge(Long value) {

        }
    }

    public static class Response {
        public record Point(
                String userId,
                Long value
        ) {
            public static Point of(String userId, Long value) {
                return new Point(userId, value);
            }

        }
    }
}
