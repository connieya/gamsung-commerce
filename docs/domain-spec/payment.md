# 결제 (Payment) Specification

## 결제 플로우의 멱등성 (Idempotency) 보장
- **Context**: 네트워크 재시도 및 사용자 중복 클릭에 따른 중복 결제/처리 방지
- **Specification**:
  - **Key**: `orderNo` + `orderKey` + `operationType` 조합
  - **Mechanism**: `IdempotencyKey` 엔티티에 처리 결과(JSON)를 저장하여 동일 요청 시 재연산 없이 반환
  - **Scope**: `ready`, `payment-session` 단계에 적용
- **Implementation**: `IdempotencyKey`, `PaymentService`
