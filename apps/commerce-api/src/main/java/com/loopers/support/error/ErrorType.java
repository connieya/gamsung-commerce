package com.loopers.support.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {
    /**
     * 범용 에러
     */
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "일시적인 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "잘못된 요청입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "존재하지 않는 요청입니다."),
    CONFLICT(HttpStatus.CONFLICT, HttpStatus.CONFLICT.getReasonPhrase(), "이미 존재하는 리소스입니다."),


    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User Already Exists", "이미 존재하는 사용자입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User Not Found", "존재하지 않는 사용자 입니다."),

    // 포인트 관련 에러
    POINT_INVALID_CHARGE_AMOUNT(HttpStatus.BAD_REQUEST, "Point Invalid Charge Amount", "충전할 포인트는 양수여야 합니다."),
    POINT_INSUFFICIENT(HttpStatus.BAD_REQUEST, "Point Insufficient", "포인트가 부족합니다."),

    BRAND_NOT_FOUND(HttpStatus.NOT_FOUND, "Brand Not Found", "존재하지 않는 브랜드입니다."),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product Not Found", "존재하지 않는 상품입니다."),

    STOCK_INSUFFICIENT(HttpStatus.BAD_REQUEST, "Stock Insufficient", "재고가 부족합니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order Not Found", "존재하지 않는 주문입니다."),
    ORDER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "Order Invalid Status", "주문 상태가 결제를 진행할 수 없는 상태입니다."),
    ORDER_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "Order Invalid Amount", "최종 결제 금액이 0 이하일 수 없습니다."),

    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND , "Coupon Not Found", "존재하지 않는 쿠폰입니다."),
    USER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND , "UserCoupon Not Found", "존재하지 않는 사용자 쿠폰입니다."),
    USER_COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "UserCoupon Already Used", "이미 사용된 쿠폰입니다."),


    LIKE_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "Like Summary Not Found", "좋아요 요약 정보를 찾을 수 없습니다."),
    LIKE_COUNT_CANNOT_BE_NEGATIVE(HttpStatus.BAD_REQUEST, "Like Count Cannot Be Negative", "좋아요 개수는 0 미만이 될 수 없습니다."),

    ;


    private final HttpStatus status;
    private final String code;
    private final String message;
}
