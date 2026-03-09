# 리뷰 (Review) Specification

## 문서 목적
- 리뷰 도메인의 비즈니스 규칙과 API 스펙을 정의한다.
- 무신사 레퍼런스 기준 상품(goods) 도메인에 포함되는 기능이지만, 우리 프로젝트에서는 commerce-api의 독립 패키지로 구현한다.

## 핵심 비즈니스 규칙

### 리뷰 작성 조건
- **구매 확정(COMPLETED)** 상태인 주문의 상품에만 리뷰를 작성할 수 있다.
- 하나의 상품에 대해 사용자당 **1건만** 작성 가능하다.
- 구매 검증은 order-api에 Feign 호출로 수행한다.

### 평점
- 1~5 사이 정수값만 허용한다.
- 상품별 평균 평점과 리뷰 수를 집계하여 제공한다.

### 리뷰 수정/삭제
- 작성자 본인만 수정/삭제 가능하다.
- 삭제는 soft delete (논리적 삭제)로 처리한다.

## 도메인 모델

### Review 엔티티

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | Long | PK (auto increment) |
| `userId` | Long | 작성자 ID |
| `productId` | Long | 상품 ID |
| `orderId` | Long | 주문 ID |
| `rating` | int | 평점 (1~5) |
| `content` | String | 리뷰 내용 (최대 1000자) |
| `createdAt` | ZonedDateTime | 작성일시 |
| `updatedAt` | ZonedDateTime | 수정일시 |
| `deletedAt` | ZonedDateTime | 삭제일시 (soft delete) |

## API 스펙

### 리뷰 작성 — `POST /api/v1/reviews?userId={userId}`
- **Request Body**:
  ```json
  {
    "productId": 1,
    "orderId": 1,
    "rating": 5,
    "content": "좋은 상품입니다."
  }
  ```
- **서버 처리**:
  1. order-api에 주문 조회 (Feign)
  2. 주문 상태 COMPLETED 확인 + 주문자/상품 검증
  3. 중복 리뷰 확인
  4. 리뷰 생성

### 리뷰 단건 조회 — `GET /api/v1/reviews/{reviewId}`

### 상품 리뷰 목록 — `GET /api/v1/reviews/products/{productId}?page=0&size=10`
- 최신순 정렬
- 평점 집계(평균, 총 건수) 함께 반환

### 리뷰 수정 — `PUT /api/v1/reviews/{reviewId}?userId={userId}`
- **Request Body**:
  ```json
  {
    "rating": 4,
    "content": "수정된 리뷰 내용"
  }
  ```

### 리뷰 삭제 — `DELETE /api/v1/reviews/{reviewId}?userId={userId}`
- soft delete 처리

### 상품 평점 조회 — `GET /api/v1/reviews/products/{productId}/rating`
- 평균 평점, 리뷰 수 반환

## 구매 검증 흐름

```text
ReviewFacade.createReview()
  └─ OrderVerifier.verifyPurchase(orderId, userId, productId)
       └─ OrderApiClient.getOrder(orderId)  ← Feign 호출
            ├─ 주문 상태 == COMPLETED 확인
            ├─ 주문자 == userId 확인
            └─ 주문 상품에 productId 포함 확인
  └─ ReviewService.create(command)
       ├─ 중복 리뷰 확인 (userId + productId)
       └─ Review 엔티티 생성·저장
```

## 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `REVIEW_NOT_FOUND` | 404 | 존재하지 않는 리뷰 |
| `REVIEW_ALREADY_EXISTS` | 409 | 이미 해당 상품에 리뷰 작성됨 |
| `REVIEW_NOT_OWNER` | 403 | 리뷰 작성자가 아님 |
| `REVIEW_ORDER_NOT_COMPLETED` | 400 | 구매 확정되지 않은 주문 |
| `REVIEW_INVALID_RATING` | 400 | 평점 범위(1~5) 초과 |
