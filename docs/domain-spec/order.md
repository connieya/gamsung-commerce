# 주문 (Order) Specification

## 주문번호 (Order Number) 설계
- **Context**: 고객 응대 및 외부 시스템 추적을 위한 가독성 있는 식별자 확보
- **Specification**:
  - **Format**: 18자리 숫자 (`yyyyMMddHHmmss` + 4자리 랜덤 일련번호)
  - **Example**: `202602162217020004`
  - **Lifecycle**: 결제 진입 전 `POST /api/v1/orders/order-no`를 통해 선발급
- **Implementation**: `OrderNumberGenerator`, `OrderNoIssuer`

## 주문·결제 플로우 (무신사 스타일)
- **Context**: 실제 서비스 분석을 통한 안정적인 주문/결제 단계 분리
- **Specification**:
  1. **Issue OrderNo**: 주문번호 및 검증 토큰 발급
  2. **Payment Ready**: 결제 데이터 생성 및 상태 검증
  3. **Create Session**: PG 결제 세션 생성
- **Implementation**: `OrderNoIssuer`, `PaymentService`
