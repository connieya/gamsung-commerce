# 주문 (Order) Specification

## 주문번호 (Order Number) 설계
- **Context**: 고객 응대 및 외부 시스템 추적을 위한 가독성 있는 식별자 확보
- **Specification**:
  - **Format**: 18자리 숫자 (`yyyyMMddHHmmss` + 4자리 랜덤 일련번호)
  - **Example**: `202602162217020004`
  - **Lifecycle**: 결제 진입 전 `POST /api/v1/orders/order-no`를 통해 선발급
- **Implementation**: `OrderNumberGenerator`, `OrderNoIssuer`

## 주문번호 사전 발급 및 서명 검증
- **Context**: 결제 전 주문번호를 확정하고, 위변조·이중 결제를 방지하기 위한 서명 체계
- **Specification**:
  - **발급 시점**: 사용자가 "결제하기" 클릭 시, 실제 PG 호출 전에 서버에서 주문번호와 보안 토큰을 생성
  - **발급 데이터**:
    | 필드 | 설명 |
    |------|------|
    | `orderNo` | 주문번호 (18자리) |
    | `orderSignature` | HMAC-SHA256 서명값 (위변조 방지) |
    | `timestamp` | 서명 생성 시각 (ms) |
    | `orderVerifyKey` | 4자리 확인 코드 |
    | `orderKey` | 32자리 hex 고유 키 (PG 전달용) |
  - **서명 생성**: `HMAC-SHA256(orderNo:timestamp:orderKey:verifyKey, secret)`
  - **서버 저장**: `IssuedOrderNo` 엔티티에 발급 정보 영구 저장 (`used` 플래그로 사용 여부 관리)
  - **검증 시점**: `POST /api/v1/orders` (주문 생성) 진입 시 서명 재생성 후 비교
    - 미발급 주문번호 → `ORDER_NO_NOT_ISSUED`
    - 이미 사용된 번호 → `ORDER_NO_ALREADY_USED`
    - 서명 불일치 → `ORDER_SIGNATURE_INVALID`
- **Implementation**: `OrderNoIssuer`, `OrderNoIssuerImpl`, `IssuedOrderNo`, `IssuedOrderNoRepository`

## 주문·결제 전체 플로우 (무신사 네트워크 분석 기반)

### Step 1. 주문서 초기화 — `GET /order/order-form?t={timestamp}`

- **Context**: 상품 상세에서 "구매하기" 클릭 → 주문서 페이지 진입 시 호출
- **Specification**:
  - `t` 쿼리 파라미터: 캐시 방지용 타임스탬프
  - 서버가 주문서 렌더링에 필요한 **모든 데이터를 한 번에 반환**
  - 응답 데이터:
    | 필드 | 설명 |
    |------|------|
    | `member` | 회원 정보 (이름, 연락처, 등급, 보유 포인트, 적립률) |
    | `defaultAddress` | 기본 배송지 (수령인, 주소, 배송 메모) |
    | `cartItems[]` | 주문 상품 목록 (상품명, 옵션, 수량, 정가/할인가, 브랜드, 도착예정일) |
    | `promotion` | 프로모션 조건 (최소 결제금액, 추가 적립률) |
    | `payUrl` | 결제 요청 엔드포인트 (서버 동적 지정) |
    | `payCheckUrl` | 결제 상태 확인 엔드포인트 |
    | `featureFlag` | 기능 플래그 |
  - `cartItems`에 정가(`normalPrice`)와 할인가(`salePrice`, 할인율 포함) 모두 포함
  - `arrivalInfo`로 도착보장 정보 제공
- **무신사 특징**: `payUrl`을 서버가 동적으로 내려줌 → 프론트에 결제 엔드포인트를 하드코딩하지 않음

### Step 2. 주문번호 발급 — `POST /api/v1/orders/order-no`

- **Context**: "결제하기" 버튼 클릭 시 가장 먼저 호출. PG 호출 전 주문 식별 정보 확정
- **Specification**:
  - **Request**: `{ isNewOrderForm: true }`
  - **Response**:
    ```json
    {
      "orderNo": "202602171816350003",
      "orderSignature": "zBMGcCrBRtq3ruDVX7yfKhHP77SzWPDRE9lVmWiNZTqxrqE5e5fM2jjP/mpEdcuV",
      "timestamp": 1771319795517,
      "orderVerifyKey": "5445",
      "orderKey": "18a194afefe47201b9398c7c9bdefd8c"
    }
    ```
  - `isNewOrderForm`: 새 주문 vs 재주문/재시도 구분 플래그 (현재 로직 미사용, 스키마 호환용)
  - 발급된 정보는 `IssuedOrderNo` 테이블에 저장
- **목적**: 주문번호 확정, 위변조 방지 서명 생성, 이중 결제 방지, 타임아웃 관리

### Step 3. 결제 준비 — `POST /api/v1/orders/{orderNo}/ready`

- **Context**: 발급된 주문번호로 결제 데이터를 서버에 등록. 주문 상태 검증 후 Payment/PaymentAttempt 생성
- **Specification**:
  - **Request** (무신사 기준 주요 필드):
    | 필드 | 설명 |
    |------|------|
    | `ord_no` / `orderNo` | 발급된 주문번호 |
    | `orderSignature` | 서명값 |
    | `ord_key` | 주문 키 |
    | `timestamp` | 발급 타임스탬프 |
    | `ord_verify_key` | 확인 코드 |
    | `pay_kind` | 결제 수단 (`TOSSPAY`, `CARD`, `BANK` 등) |
    | `pgKind` | PG사 종류 (`toss`, `kcp` 등) |
    | `good_name` | 대표 상품명 |
    | `good_mny` | 결제 금액 |
    | `orderAmount` | 금액 상세 (정가, 할인가, 결제액, 배송비) |
    | `discount` | 할인 상세 (포인트, 쿠폰, 장바구니 쿠폰) |
    | `recipient` | 수령인 정보 (이름, 주소, 연락처, 배송 메모) |
    | `cashReceipt` | 현금영수증 정보 |
  - **Response**: `{ "mpToken": null }` — 간소한 응답 (무신사페이 토큰, 비사용 시 null)
  - **서버 처리**:
    1. `orderNo`로 주문 조회 → 상태 검증 (`INIT`만 허용)
    2. `Payment` 엔티티 생성 (PENDING)
    3. `PaymentAttempt` 생성 (REQUESTED)
    4. `IdempotencyKey` 저장 (READY 타입)
  - **멱등성**: 동일 `orderNo + orderKey + READY` 조합으로 재요청 시 캐시된 결과 반환
- **무신사 특징**: 전체 주문 정보(상품, 금액, 배송지, 할인, 현금영수증)를 이 단계에서 모두 전달

### Step 4. 결제 세션 생성 — `POST /api/v1/orders/payment-session`

- **Context**: PG사(토스 등)에 결제 요청을 보내고, 사용자를 PG 결제 페이지로 리다이렉트하기 위한 URL 획득
- **Specification**:
  - **Request** (무신사 기준 주요 필드):
    | 필드 | 설명 |
    |------|------|
    | `orderNo` | 주문번호 |
    | `payKind` | 결제 수단 (`TOSSPAY`) |
    | `payAmount` | 결제 금액 |
    | `member` | 결제자 정보 (이름, 이메일, 연락처) |
    | `goods[]` | 상품 목록 |
    | `cashReceipt` | 현금영수증 |
    | `taxFreeAmount` | 면세 금액 |
    | `cardQuota` | 할부 개월 수 |
  - **Response**:
    ```json
    {
      "orderNo": "202602171816350003",
      "paymentKey": "PP5STE6217TOSVI420150952345763",
      "amount": 27900,
      "paymentUrl": "https://pay.musinsapayments.com/mps/payment/PP5STE6217TOSVI420150952345763",
      "pgTid": null,
      "pgKind": "VIVAREPUBLICA",
      "virtualAccount": null
    }
    ```
  - **서버 처리**:
    1. `orderNo`로 주문 조회 → 상태 검증
    2. PG사에 결제 요청 → `paymentKey`, `paymentUrl` 수신
    3. `IdempotencyKey` 저장 (PAYMENT_SESSION 타입)
    4. `PaymentEvent.Complete` 이벤트 발행
  - **멱등성**: 동일 `orderNo + orderKey + PAYMENT_SESSION` 조합으로 재요청 시 캐시된 결과 반환
  - **프론트 후속 처리**: `paymentUrl`로 사용자를 리다이렉트 → PG 결제 화면

### Step 5. 결제 완료 확인 — `Ret_URL` 콜백 + 브라우저 리다이렉트

- **Context**: PG 결제 완료 후 **서버 채널**(결제 확인)과 **브라우저 채널**(화면 전환) 두 경로가 동시 발생
- **Specification**:
  - **콜백 URL**: Step 3 ready 요청 시 `Ret_URL` 필드로 PG사에 전달
    - 무신사 실제값: `https://order.musinsa.com/api2/order/v1/orders/payment-check`
  - **서버 채널** (PG → 서버):
    1. PG사가 `Ret_URL`로 결제 결과 전달 (`transactionKey`, `orderNo`, 결제 상태)
    2. `PaymentService.complete()`: PG사 트랜잭션 상세 조회 후 Payment 상태 갱신
    3. `OrderEventListener.onPaymentSuccess()`: Payment 성공 이벤트 수신 → Order 상태를 `PAID`로 변경
  - **브라우저 채널** (PG → 브라우저):
    1. PG사가 브라우저를 주문 완료 페이지로 리다이렉트
    2. 무신사 실제 URL: `/orders/order/order_result/{orderNo}?code=0`
    3. `code=0`: 결제 성공 / 그 외: 실패·취소
  - **두 채널의 관계**: 서버 채널이 상태 갱신의 **신뢰 기준** (source of truth), 브라우저 채널은 화면 전환 역할만 수행

### 전체 시퀀스

```
사용자               프론트엔드                 백엔드 API              PG사
  │                    │                        │                    │
  │─ 구매하기 클릭 ──→│                        │                    │
  │                    │── GET /order-form ───→│                    │
  │                    │←── 주문서 데이터 ──────│                    │
  │                    │                        │                    │
  │─ 결제하기 클릭 ──→│                        │                    │
  │                    │── POST /order-no ────→│                    │
  │                    │←── orderNo+서명 ───────│                    │
  │                    │                        │                    │
  │                    │── POST /{no}/ready ──→│                    │
  │                    │←── mpToken ────────────│                    │
  │                    │                        │                    │
  │                    │── POST /payment-session→│                   │
  │                    │←── paymentUrl ─────────│                    │
  │                    │                        │                    │
  │                    │── redirect paymentUrl ──────────────────→│
  │                    │                        │                    │
  │─ QR스캔/결제승인 ────────────────────────────────────────→│
  │                    │                        │                    │
  │                    │                        │←── Ret_URL 콜백 ──│
  │                    │                        │  (결제결과 전달)    │
  │                    │                        │                    │
  │                    │                  ┌─────┤                    │
  │                    │                  │ Payment → PAID           │
  │                    │                  │ Order → PAID (이벤트)    │
  │                    │                  └─────┤                    │
  │                    │                        │                    │
  │                    │←── 302 Redirect ───────────────────────────│
  │                    │  /order_result/{orderNo}?code=0            │
  │                    │                        │                    │
  │←── 주문 완료 페이지─│                       │                    │
  │                    │                        │                    │
```

## 주문 상태 (OrderStatus)

| 상태 | 설명 | 전이 조건 |
|------|------|----------|
| `INIT` | 주문 생성 직후 | 주문 생성 시 |
| `PENDING` | 결제 시도 중 (가상계좌 대기 등) | ready 호출 시 |
| `PAID` | 결제 완료 | PG 콜백 성공 시 |
| `FAILED` | 주문 실패 (타임아웃, 재고 부족) | PG 콜백 실패 시 |
| `CANCELED` | 취소 (환불 포함) | 취소 요청 시 |
| `COMPLETED` | 구매 확정 | 배송 완료 후 자동/수동 확정 |

```
INIT → PENDING → PAID → COMPLETED
                    ↘ FAILED
               CANCELED ↙
```
