package com.loopers.domain.order;

public enum OrderStatus {
    INIT, //  사용자가 주문서를 작성하고 '결제하기' 를 누른 직후의 상태,  아직 결제는 완료되지 않았음
    PENDING,     // 결제 시도했으나 최종 결과(승인/실패)가 나지 않은 상태 (예: 가상계좌 입금 대기, 인증 대기)
    PAID, // PG 사로부터 콜백 URL 를 통해 결제 완료 통보를 받은 상태
    FAILED, //  결제 시간 초과, 재고 부족 등으로 주문이 실패 처리 된 상태
    CANCELED, //  결제 완료 후 환불을 동반하여 주문이 취소 된 상태
    COMPLETED //  고객이 '구매 확정' 버튼을 누르거나,  배송 완료 후 일정 기간이 지나 자동 확정된 상태.
}
