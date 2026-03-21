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

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product Not Found", "존재하지 않는 상품입니다."),

    // SKU / 옵션 관련 에러
    SKU_NOT_FOUND(HttpStatus.NOT_FOUND, "Sku Not Found", "존재하지 않는 SKU입니다."),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Option Not Found", "존재하지 않는 옵션입니다."),
    DUPLICATE_SKU_OPTION_COMBINATION(HttpStatus.CONFLICT, "Duplicate Sku Option Combination", "동일한 옵션 조합의 SKU가 이미 존재합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
