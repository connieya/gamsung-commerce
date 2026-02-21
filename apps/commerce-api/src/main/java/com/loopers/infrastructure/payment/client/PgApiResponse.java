package com.loopers.infrastructure.payment.client;

public record PgApiResponse<T>(Metadata meta, T data) {
    public record Metadata(String result, String errorCode, String message) {}
}
