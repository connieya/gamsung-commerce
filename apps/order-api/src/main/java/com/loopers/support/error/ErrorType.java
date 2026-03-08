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

    STOCK_INSUFFICIENT(HttpStatus.BAD_REQUEST, "Stock Insufficient", "재고가 부족합니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order Not Found", "존재하지 않는 주문입니다."),
    ORDER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "Order Invalid Status", "주문 상태가 결제를 진행할 수 없는 상태입니다."),
    ORDER_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Order Invalid Amount", "최종 결제 금액이 0 이하일 수 없습니다."),
    ORDER_NO_NOT_ISSUED(HttpStatus.BAD_REQUEST, "Order No Not Issued", "발급되지 않은 주문번호입니다."),
    ORDER_SIGNATURE_INVALID(HttpStatus.BAD_REQUEST, "Order Signature Invalid", "주문 서명이 유효하지 않습니다."),
    ORDER_NO_ALREADY_USED(HttpStatus.CONFLICT, "Order No Already Used", "이미 사용된 주문번호입니다."),

    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Cart Item Not Found", "장바구니 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
