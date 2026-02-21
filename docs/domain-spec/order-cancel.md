# 주문 취소 (Order Cancel) Specification

## 취소 플로우 개요

무신사는 취소/반품/교환을 **클레임(claim) 도메인**으로 분리 운영하며, CQRS 패턴을 적용한다.
- 조회: `/claim/store/...`
- 실행: `/claim/command/...`

## 페이지 전환 흐름

```
1. 주문 상세
   /order/order-detail/{orderNo}

2. 주문 취소 신청
   /order/claim/order-cancel/{orderNo}/{orderOptionNo}?orderCancelType=IS_AFTER_PAYMENT

3. 주문 취소 완료
   /order/claim/order-cancel/complete/{orderNo}?orderOptionNoList={orderOptionNo}&orderCancelType=IS_AFTER_PAYMENT
```

### URL 파라미터

| 파라미터 | 예시 | 설명 |
|----------|------|------|
| `orderNo` | `202602211035060001` | 주문번호 |
| `orderOptionNo` | `405156561` | 주문 옵션 번호 (상품+옵션 조합 단위) |
| `orderCancelType` | `IS_AFTER_PAYMENT` | 취소 유형 — 결제 후 취소 (↔ 결제 전 취소 별도 존재 추정) |
| `orderOptionNoList` | `405156561` | 취소 대상 목록 (복수 부분 취소 가능) |

## 취소 API 호출 순서

```
주문 상세 페이지
  │
  ├── (1) GET  /order-items/{optionNo}/status
  │        취소 가능 여부 사전 확인
  │
  ↓ 취소 버튼 클릭 → 취소 신청 페이지
  │
  ├── (2) POST /claim/store/.../refund/payment/calculator
  │        환불 금액 미리보기
  ├── (3) POST /order-service/.../check_refundable_new_member_event_goods
  │        이벤트 상품 제약 확인
  │
  ↓ 취소 확인 클릭
  │
  ├── (4) POST /claim/store/.../check/order/status
  │        상태 최종 확인 (낙관적 락)
  ├── (5) POST /claim/command/.../order_cancel_cmd/refund
  │        취소 + 환불 실행
  │
  ↓ 취소 완료 페이지
```

## 각 API 상세

### (1) 주문 아이템 상태 조회 — `GET /order-items/{orderOptionNo}/status`

- **도메인**: `order.musinsa.com`
- **식별자**: `orderOptionNo` (주문 전체가 아닌 개별 아이템 단위)
- **목적**: 해당 아이템이 취소 가능한 상태인지 확인 (배송 중이면 취소 불가 등)

### (2) 환불 금액 계산 — `POST /claim/store/mypage/refund/payment/calculator`

- **도메인**: `api.musinsa.com`
- **목적**: 취소 시 환불 예정 금액을 사전 계산하여 사용자에게 표시
- **Request**:
  | 필드 | 예시 | 설명 |
  |------|------|------|
  | `orderNo` | `202602211035060001` | 주문번호 |
  | `returnDeliveryZipcode` | `""` | 반품 배송 우편번호 (취소 시 빈값, 반품 시 사용) |
  | `selectedOrderList` | `[{ orderOptionNo, quantity }]` | 취소 대상 아이템 목록 |
- **Response 추정**: 환불 금액 상세 (상품금액, 포인트 환급, 쿠폰 복원, 배송비 등)
- **`selectedOrderList` 배열**: 여러 아이템 부분 취소 가능, `quantity` 지정으로 수량 단위 부분 취소도 가능
- **반품과 API 공유**: `returnDeliveryZipcode`가 있는 것으로 보아 반품 시에는 반송 배송비 계산에도 동일 API 사용

### (3) 신규 회원 이벤트 상품 확인 — `POST /order-service/api/mypage/check_refundable_new_member_event_goods`

- **도메인**: `www.musinsa.com`
- **Content-Type**: Form Data
- **목적**: 신규 회원 이벤트로 받은 상품이 취소 대상에 포함되어 있는지 검증
- **Request**: `ord_no`, `ord_opt_nos`
- **비고**: 레거시 API (Form Data, 스네이크케이스 필드명). 우리 프로젝트에서는 당장 불필요

### (4) 주문 상태 최종 확인 — `POST /claim/store/mypage/check/order/status`

- **도메인**: `api.musinsa.com`
- **경로**: `/claim/store/...` (CQRS 조회 측)
- **목적**: 취소 실행 직전, 주문 상태가 여전히 취소 가능한지 최종 확인
- **비고**: 사용자가 취소 화면에 머무르는 동안 다른 경로(CS, 시스템)로 상태가 변경될 수 있으므로, 실행 직전에 한번 더 확인하는 낙관적 락 역할

### (5) 취소 + 환불 실행 — `POST /claim/command/mypage/order_cancel_cmd/refund`

- **도메인**: `api.musinsa.com`
- **경로**: `/claim/command/...` (CQRS 실행 측)
- **목적**: 주문 취소 + PG 환불 동시 실행
- **Request** (Form Data):
  | 필드 | 예시 | 설명 |
  |------|------|------|
  | `ord_no` | `202602211035060001` | 주문번호 |
  | `ord_opt_nos` | `405156561` | 취소 대상 옵션 번호 |
  | `refund_bank` | (빈값) | 환불 은행 (PG 결제 시 불필요) |
  | `refund_account` | (빈값) | 환불 계좌 (PG 결제 시 불필요) |
  | `refund_nm` | (빈값) | 예금주 (PG 결제 시 불필요) |
  | `claim_reason` | `21` | 취소 사유 코드 (드롭다운 선택값) |
  | `cancel_content` | (빈값) | 취소 상세 사유 (자유 입력) |
- **환불 계좌 필드**: 카카오페이 등 PG 결제 시 원래 결제수단으로 자동 환불되므로 빈값. 무통장입금 결제 시에는 환불 계좌 필수
- **서버 처리 추정**:
  1. 주문 상태 검증 (`PAID`만 허용)
  2. Payment 환불 요청 (PG사 환불 API 호출)
  3. Order 상태 → `CANCELED`
  4. 포인트/쿠폰 복원 처리

## 아키텍처 관점 정리

### CQRS 패턴
- **조회 (store)**: `/claim/store/mypage/...` — 상태 확인, 환불 계산
- **실행 (command)**: `/claim/command/mypage/...` — 취소/환불 실행

### 취소 단위
- 주문 전체가 아닌 **`orderOptionNo` 단위** (개별 상품 옵션)로 취소
- `selectedOrderList` / `orderOptionNoList`로 복수 선택 가능 → 부분 취소 지원
- `quantity` 지정으로 동일 옵션의 수량 단위 부분 취소도 가능

### 취소 전 다단계 검증
| 순서 | 검증 | 목적 |
|------|------|------|
| 1 | 아이템 상태 조회 | 취소 가능 상태인지 |
| 2 | 환불 금액 계산 | 환불 예정 금액 사용자 확인 |
| 3 | 이벤트 상품 확인 | 이벤트 혜택 회수 여부 |
| 4 | 상태 최종 확인 | 실행 직전 낙관적 락 |

### 취소 유형
- `IS_AFTER_PAYMENT`: 결제 완료 후 취소 (환불 수반)
- 결제 전 취소 유형도 별도 존재 추정 (URL 파라미터 구조상)
