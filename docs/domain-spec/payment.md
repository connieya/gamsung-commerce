# 결제 (Payment) Specification

## 결제 플로우의 멱등성 (Idempotency) 보장
- **Context**: 네트워크 재시도 및 사용자 중복 클릭에 따른 중복 결제/처리 방지
- **Specification**:
  - **Key**: `orderNo` + `orderKey` + `operationType` 조합 (Unique Constraint)
  - **Mechanism**: `IdempotencyKey` 엔티티에 처리 결과(JSON)를 저장하여 동일 요청 시 재연산 없이 반환
  - **Scope**: `READY`, `PAYMENT_SESSION` 두 단계에 적용
  - **Race Condition 대응**: `DataIntegrityViolationException` catch로 동시 요청 시 중복 저장 방지
  - **결과 캐싱**: `resultData` (TEXT) 필드에 JSON 직렬화하여 저장
- **Implementation**: `IdempotencyKey`, `PaymentService.saveIdempotencyKey()`

## 결제 준비 (Payment Ready)
- **Context**: PG 결제 전 서버에 Payment/PaymentAttempt 레코드를 생성하여 결제 추적 기반 확보
- **Specification**:
  - **Trigger**: `POST /api/v1/orders/{orderNo}/ready`
  - **처리 순서**:
    1. 멱등성 체크 (`READY` 타입 IdempotencyKey 조회)
    2. 이미 존재하면 캐시된 `PaymentReadyResult` 반환
    3. 미존재 시:
       - `Payment` 생성 (상태: `PENDING`)
       - `PaymentAttempt` 생성 (상태: `REQUESTED`)
       - `IdempotencyKey` 저장
  - **입력**: 주문 금액, 결제 수단, 사용자 정보, 주문 키
  - **출력**: `PaymentReadyResult(paymentId, paymentStatus)`
  - **`Ret_URL` 전달**: PG 결제 완료 후 콜백 URL이 이 단계에서 서버에 전달됨 (무신사 실제값: `https://order.musinsa.com/api2/order/v1/orders/payment-check`)
  - **금액 정합성 검증**: 프론트에서 전달한 `orderAmount`, `discount` 값을 서버가 재계산·검증해야 함 (위변조 방지)
- **Implementation**: `PaymentService.ready()`, `PaymentFacade.ready()`

## 결제 세션 생성 (Payment Session)
- **Context**: PG사에 결제를 요청하고 사용자가 결제를 진행할 수 있는 URL을 확보
- **Specification**:
  - **Trigger**: `POST /api/v1/orders/payment-session`
  - **처리 순서**:
    1. 멱등성 체크 (`PAYMENT_SESSION` 타입 IdempotencyKey 조회)
    2. 이미 존재하면 캐시된 `PaymentSessionResult` 반환
    3. 미존재 시:
       - `PaymentClient.request()` → PG사 결제 요청
       - `paymentUrl` 생성 (PG 결제 페이지 URL)
       - `IdempotencyKey` 저장
       - `PaymentEvent.Complete` 이벤트 발행
    4. PG 요청 실패 시:
       - `PaymentEvent.Failure` 이벤트 발행
       - 예외 전파
  - **입력**: 주문번호, 결제수단, 결제금액, 결제자 정보, 상품 목록
  - **출력**: `PaymentSessionResult(orderNo, paymentKey, amount, paymentUrl, pgKind)`
  - **`paymentKey` 포맷**: PG사 식별자가 키에 포함됨
    | payKind | pgKind | paymentKey 예시 | 키 내 식별자 |
    |---------|--------|----------------|-------------|
    | `KAKAOPAY` | `KAKAO` | `PP5STE6221KAK001...` | `KAK` |
    | `TOSSPAY` | `VIVAREPUBLICA` | `PP5STE6217TOSVI4...` | `TOSVI` |
  - **`paymentUrl`**: 무신사 자체 결제 게이트웨이(`pay.musinsapayments.com`) 경유
  - **ready와의 역할 분리**: ready에서 주문 전체 정보(금액, 배송지, 할인 등)를 등록하고, payment-session은 PG 요청에 필요한 최소 정보(결제수단, 금액, 결제자)만 전달
- **Implementation**: `PaymentService.createPaymentSession()`, `PaymentFacade.createPaymentSession()`

## 결제 완료 확인 (Payment Complete)
- **Context**: PG 결제 완료 후 서버가 트랜잭션 결과를 확인하고 상태를 갱신
- **Specification**:
  - **두 가지 경로로 결제 결과 수신**:
    | 경로 | Trigger | 역할 |
    |------|---------|------|
    | 서버 채널 | PG → `Ret_URL` (`/orders/payment-check`) | PG 서버 간 직접 통신 (보조 확인) |
    | 브라우저 채널 | PG → 브라우저 → `POST /orders/process` | 원본 주문 데이터 + PG 결과를 합쳐 최종 처리 |
  - **PG 결과 검증 항목** (`/orders/process` 수신 시):
    1. `res_cd == "0000"` 확인 (PG 결제 성공 여부)
    2. PG `amount`와 서버 보관 `payAmount` 일치 검증 (금액 위변조 방지)
    3. `auth_token` (JWT), `enc_code`로 PG 결제 진위 검증
    4. `payment_key`로 PG 트랜잭션 매칭
  - **처리 순서**:
    1. PG 결과 검증 통과
    2. Payment 상태 갱신: `PENDING` → `PAID` (또는 `FAILED`)
    3. 이벤트 발행 → Order 상태 전이
  - **이벤트 연동**: `PaymentEvent.Success` → `OrderEventListener.onPaymentSuccess()` → Order `PAID` 상태 변경
- **Implementation**: `PaymentService.complete()`, `OrderEventListener`

## Payment 상태

| 상태 | 설명 |
|------|------|
| `PENDING` | 결제 요청됨, 결과 대기 중 |
| `PAID` | 결제 성공 |
| `FAILED` | 결제 실패 |

## PaymentAttempt 상태 (AttemptStatus)

| 상태 | 설명 |
|------|------|
| `REQUESTED` | PG 요청 전송 |
| `FAILED` | PG 요청 실패 |

## IdempotencyKey 엔티티

```
idempotency_keys
├── order_no         (주문번호)
├── order_key        (주문 키)
├── operation_type   (READY / PAYMENT_SESSION)
├── result_data      (JSON 캐시 결과)
└── UNIQUE(order_no, order_key, operation_type)
```
