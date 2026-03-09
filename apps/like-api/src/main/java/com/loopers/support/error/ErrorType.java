package com.loopers.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), "이미 존재하는 리소스입니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found", "존재하지 않는 사용자 입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product Not Found", "존재하지 않는 상품입니다."),

    LIKE_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "Like Summary Not Found", "좋아요 요약 정보를 찾을 수 없습니다."),
    LIKE_COUNT_CANNOT_BE_NEGATIVE(HttpStatus.BAD_REQUEST, "Like Count Cannot Be Negative", "좋아요 개수는 0 미만이 될 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
