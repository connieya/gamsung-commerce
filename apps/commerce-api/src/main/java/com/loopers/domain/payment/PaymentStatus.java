package com.loopers.domain.payment;

public enum PaymentStatus {
    PENDING, // 우리 시스템에 Payment 객체가 생성되었지만, 아직 사용자가 실제 결제를 시작하지 않은 초기 상태.
    PAID, //  PG사로부터 결제 성공 통보를 받은 상태.
    FAILED, // 결제 과정에서 오류가 발생하거나 실패한 상태. 예: 카드 한도 초과, 결제 시스템 오류 등
    CANCELLED // 결제가 완료되기 전에 사용자나 시스템에 의해 중단된 상태. 예: 사용자가 결제창 닫기, 결제 시간 초과 등
}
