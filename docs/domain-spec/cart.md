# 장바구니 (Cart) Specification

## 문서 목적
- 무신사 장바구니 흐름을 **레퍼런스**로 삼되, 우리 프로젝트에서 실제로 유지하는 최소 스펙을 정의한다.
- 아래 "프로젝트 구현 스펙" 섹션이 실제 코드 기준이며, 그 뒤의 "무신사 레퍼런스"는 참고용이다.

---

## 프로젝트 구현 스펙

### 도메인 모델

#### Cart (Aggregate Root)
- `userId` 기반 1:1 매핑 (`userId` unique)
- `CartItem`과 1:N 관계 (`cascade = ALL`, `orphanRemoval = true`)
- `getTotalAmount()`: 전체 아이템 소계 합산

#### CartItem
- `productId`, `quantity`, `price` 필드
- `getSubtotal()`: `price * quantity` 계산
- `updateQuantity()`: 수량 변경

#### ProductPort
- 외부 상품 서비스(commerce-api) 연동 포트
- `getProduct(productId)` → `ProductInfo(id, name, price, imageUrl)` 반환
- 상품 추가 시 상품 존재 여부 + 가격 검증에 사용

### API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/v1/cart` | 장바구니 조회 |
| POST | `/api/v1/cart/items` | 상품 추가 |
| PUT | `/api/v1/cart/items/{itemId}` | 수량 변경 |
| DELETE | `/api/v1/cart/items/{itemId}` | 상품 삭제 |
| DELETE | `/api/v1/cart` | 장바구니 비우기 |
| GET | `/api/v1/cart/count` | 장바구니 수량 조회 |

### 비즈니스 규칙
- **동일 상품 수량 합산**: 이미 장바구니에 있는 상품을 추가하면 기존 아이템의 수량에 합산 (중복 아이템 방지)
- **상품 검증**: 상품 추가 시 `ProductPort.getProduct()`로 상품 존재 여부 및 가격 조회
- **사용자당 하나의 Cart**: `userId` unique 제약으로 보장
- **Cart 자동 생성**: 장바구니가 없는 사용자가 조회/추가 시 자동 생성

### 주문서 연동
- 주문서 진입 시 `cartItemIds`로 장바구니 아이템 선택 가능
- `CartService.getCartItemsByIds(cartItemIds, userId)`로 선택된 아이템만 조회

---

## 무신사 레퍼런스

## 장바구니 페이지 진입

### 페이지 URL
`/orders/cart`

### API 호출 구조

장바구니 메인 데이터 조회와 부가 API가 병렬 호출되는 구조. 주문서 페이지와 동일한 패턴.

#### 부가 API 목록 (병렬 호출)

| # | API | Method | 도메인 | 목적 | 비고 |
|---|-----|--------|--------|------|------|
| 1 | `/cart/v1/gifts?cartIds={ids}` | GET | cart | 사은품 정보 조회 | 장바구니 상품에 따른 증정품 확인 |
| 2 | `/order/v1/plcc?channelCode=CART` | GET | order | PLCC 카드 혜택 | 주문서에서는 `channelCode=ORDER`, 장바구니에서는 `CART` |
| 3 | `/like/api/v1/members/liketypes/goods?page=0&size=100` | GET | like | 좋아요 상품 목록 | 장바구니 상품의 하트 아이콘 상태 표시용 |
| 4 | `/cart/v1/restocks` | POST | cart | 재입고 알림 상태 조회 | 품절 상품의 재입고 알림 신청 여부 확인 |
| 5 | `/order/v2/cart-coupon` | POST | order | 적용 가능 장바구니 쿠폰 | 주문서와 동일 API, `isBestSalePrice`로 최대 할인 쿠폰 자동 표시 |
| 6 | `/review/v1/rewards/goods?goodsNo[]=...` | GET | goods | 리뷰 적립금 정보 | 주문서와 동일 API |

## 각 API 상세

### (1) 사은품 조회 — `GET /api2/cart/v1/gifts`

- `cartIds`를 쿼리 스트링으로 전달 (콤마 구분)
- 장바구니 상품에 해당하는 사은품(증정품) 반환
- "N만원 이상 구매 시 파우치 증정" 같은 프로모션용
- 해당 사은품 없으면 `items: []`

### (2) PLCC 카드 혜택 — `GET /api2/order/v1/plcc`

- 주문서와 동일 API, `channelCode`로 채널 구분
- 카드 미발급 사용자 응답 예시:
  | 필드 | 값 | 설명 |
  |------|-----|------|
  | `isCardIssued` | `false` | PLCC 카드 미발급 |
  | `discountRate` | `5` | 발급 시 할인율 |
  | `plccFirstPaymentEventAvailable` | `true` | 첫 결제 이벤트 대상 |
  | `plccFirstPaymentEvent.discountAmount` | `30000` | 첫 결제 시 3만원 할인 |
- 미발급 사용자에게 카드 발급 유도 배너 표시용

### (3) 좋아요 상품 목록 — `GET /like/api/v1/members/liketypes/goods`

- `like.musinsa.com` 별도 도메인 (MSA)
- `size=100`으로 전량 조회 후, 장바구니 상품의 `goodsNo`와 클라이언트 사이드 매칭
- 응답 필드: `relationId` (상품번호), `count` (전체 좋아요 수)
- Spring Data Pageable 표준 구조

### (4) 재입고 알림 상태 — `POST /api2/cart/v1/restocks`

- 장바구니 전체 아이템을 `cartNo`, `goodsNo`, `goodsOptionNo` 조합으로 전달
- 품절 상품의 재입고 알림 신청 여부 반환
- **옵션 단위 조회**: 같은 상품이라도 사이즈/색상별 재고가 다르므로 `goodsOptionNo` 필수
- 해당 없으면 `items: []`

### (5) 장바구니 쿠폰 — `POST /api2/order/v2/cart-coupon`

- 주문서 진입 시와 동일 API (상세 응답 구조는 [order.md](order.md) 참고)
- 장바구니 페이지에서의 차이점:
  - Request에 `discountBasePrice` 추가 (할인 기준가)
  - 상품 수가 많아 적용 가능 쿠폰 수 증가
  - `isBestSalePrice: true` — 최대 할인 쿠폰을 서버가 자동 계산하여 플래그
  - `totalDiscountAmount` — 쿠폰별 총 할인 금액 사전 계산

### (6) 리뷰 적립금 — `GET /api2/review/v1/rewards/goods`

- 주문서와 동일 API (상세는 [order.md](order.md) 참고)
- 리뷰 유형별 적립금: 일반 후기 500원, 스타일 후기 1,000원
- `boosting` 필드로 적립금 부스팅 이벤트 여부 확인

## 아키텍처 관점 정리

### MSA 도메인 분리
장바구니 페이지 하나에서 **4개 도메인**의 API를 호출:
- `cart.musinsa.com` — 장바구니 핵심 (gifts, restocks)
- `order.musinsa.com` — 주문/쿠폰 (plcc, cart-coupon)
- `like.musinsa.com` — 좋아요
- `goods.musinsa.com` — 상품/리뷰

### 주문서 페이지와의 공통 API
| API | 장바구니 | 주문서 |
|-----|---------|--------|
| `plcc` | `channelCode=CART` | `channelCode=ORDER` |
| `cart-coupon` | 동일 | 동일 |
| `rewards/goods` | 동일 | 동일 |
