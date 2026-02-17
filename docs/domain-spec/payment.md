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
  - **입력**: 주문번호, 카드 종류, 카드 번호, 결제 금액
  - **출력**: `PaymentSessionResult(orderNo, paymentKey, amount, paymentUrl, pgKind)`
  - **무신사 실제 응답 분석**:
    | 필드 | 예시 | 설명 |
    |------|------|------|
    | `paymentKey` | `PP5STE6217TOSVI420150952345763` | PG사 결제 고유 키 |
    | `paymentUrl` | `https://pay.musinsapayments.com/mps/payment/...` | 사용자 리다이렉트 URL |
    | `pgKind` | `VIVAREPUBLICA` | PG사 식별 (토스페이먼츠) |
    | `virtualAccount` | `null` | 가상계좌 정보 (해당 시에만) |
- **Implementation**: `PaymentService.createPaymentSession()`, `PaymentFacade.createPaymentSession()`

## 결제 완료 확인 (Payment Complete)
- **Context**: PG 결제 완료 후 서버가 트랜잭션 결과를 확인하고 상태를 갱신
- **Specification**:
  - **Trigger**: PG 콜백 또는 `POST /api/v1/orders/payment-check`
  - **처리 순서**:
    1. PG사 트랜잭션 상세 조회 (`PaymentClient.getTransactionDetail()`)
    2. 트랜잭션 결과에 따라 Payment 상태 갱신:
       - `SUCCESS` → `payment.paid()`
       - `FAILED` → `payment.fail()`
       - 기타 → `payment.pending()`
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
