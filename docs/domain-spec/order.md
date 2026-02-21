# 주문 (Order) Specification

## 문서 목적
- 무신사 주문 흐름을 **레퍼런스**로 삼되, 우리 프로젝트에서 실제로 유지하는 최소 스펙을 정의한다.
- 무신사 원본의 대규모 필드셋을 그대로 복제하지 않고, 주문/결제 처리에 필요한 핵심 필드만 유지한다.

## 주문번호 (Order Number) 설계
- **Format**: 18자리 숫자 (`yyyyMMddHHmmss` + 4자리 랜덤 일련번호)
- **Example**: `202602162217020004`
- **발급 시점**: 사용자가 주문서에서 결제하기를 누르면 `POST /api/v1/orders/order-no` 호출로 선발급
- **구성값**:
  - `orderNo`
  - `orderSignature`
  - `timestamp`
  - `orderVerifyKey`
  - `orderKey`

## 주문번호 발급/검증 규칙
- **서명 생성**: `HMAC-SHA256(orderNo:timestamp:orderKey:verifyKey, secret)`
- **서버 저장**: 발급 정보는 `IssuedOrderNo`에 저장하며 `used` 상태로 재사용 방지
- **검증 시점**: `POST /api/v1/orders` 주문 생성 시점
- **예외 코드**:
  - 미발급 주문번호: `ORDER_NO_NOT_ISSUED`
  - 이미 사용된 번호: `ORDER_NO_ALREADY_USED`
  - 서명 불일치: `ORDER_SIGNATURE_INVALID`

## 주문서 진입

### 1-1) 주문서 메인 데이터 — `GET /api/v1/orders/order-form?t={timestamp}`
- **목적**: 주문서 렌더링에 필요한 최소 데이터 조회
- **요청 규칙**:
  - `t`는 캐시 방지용 쿼리
  - 바로구매인 경우 `buyNowCartItemId`를 함께 전달해 해당 장바구니 아이템 1건 우선 조회
  - 일반 주문(장바구니 주문하기)은 로그인 사용자 장바구니 전체 조회
- **응답 최소 필드**:
  - `member.name`, `member.email`
  - `cartItems[]` (`cartId`, `productId`, `productName`, `quantity`, `price`, `imageUrl`)
  - `totalAmount`

### 1-2) 주문서 부가 데이터 (병렬 호출)

`order-form`과 동시에 호출되는 부가 API 목록. 하나가 실패해도 주문서 렌더링에는 영향 없음 (graceful degradation).

| # | API | 도메인 | 목적 | 비고 |
|---|-----|--------|------|------|
| 1 | `GET /plcc?channelCode=ORDER` | order | PLCC 카드 혜택 조회 | 무신사 전용 카드 할인/적립 배너 표시용 |
| 2 | `GET /cart/count` | cart | 장바구니 수량 | 헤더 장바구니 뱃지 갱신용, 주문 로직과 무관 |
| 3 | `GET /payment/event` | order | 결제 수단별 이벤트 | "토스페이 캐시백", "카드사 무이자" 등 프로모션 태그 표시 |
| 4 | `GET /money/account` | order | 머니 잔액 조회 | "보유 머니: ₩XX,XXX" 표시 및 머니 결제 옵션 활성화 판단 |
| 5 | `GET /payment/paykind?digitalProductYn=N&...` | order | 사용 가능 결제 수단 목록 | 상품 유형(실물/디지털/바우처)에 따라 결제 수단 필터링 |
| 6 | `GET /payment/paykind/recent` | order | 최근 사용 결제 수단 | 마지막 결제 수단을 기본 선택으로 세팅 (UX 편의) |
| 7 | `GET /rewards/goods?goodsNo[]=3976350` | goods | 리뷰 적립금 정보 | "리뷰 작성 시 최대 500원 적립" 안내 표시 |
| 8 | `GET /categories/display?goodsNos=3976350` | order | 상품 카테고리 표시 정보 | 카테고리별 프로모션 적용 판단 |
| 9 | `POST /promotions` | order | 적용 가능 프로모션 조회 | 상품 목록을 body로 전달, 해당 상품에 걸린 프로모션 반환 |
| 10 | `POST /v2/cart-coupon` | order | 적용 가능 장바구니 쿠폰 목록 | 쿠폰 유형·할인값·적용 조건·만료일 등 상세 정보 반환 |

- `paykind` 쿼리 파라미터: `digitalProductYn`, `digitalProductMusinsaVoucherYn`, `exclusiveMsspayGoodsYn` — 상품 속성에 따라 결제 수단 제한
- `cart/count`는 `cart.musinsa.com` 도메인으로 분리 운영 (MSA 구조)
- `rewards/goods`는 `goods.musinsa.com` 도메인으로 분리 운영
- #9, #10은 POST 요청 — 상품/장바구니 데이터를 body로 전달해야 하므로

#### 장바구니 쿠폰 응답 구조 (`cart-coupon`)

| 필드 | 예시 | 설명 |
|------|------|------|
| `couponAmountKind` | `P` / `W` | 할인 유형 — `P`: 정률(%), `W`: 정액(원) |
| `couponKind` | `%` / `원` | 표시용 단위 |
| `couponValue` | `7` / `9000` | 할인값 |
| `maxLimitAmount` | `10000` | 최대 할인 한도 (`maxLimitYn=Y`일 때 적용) |
| `lowPrice` | `50000` | 최소 주문 금액 조건 (`priceYn=Y`일 때) |
| `lowQuantity` | `2` | 최소 수량 조건 (`quantityYn=Y`일 때) |
| `isTotalAboveLowPrice` | `N` | 현재 주문이 최소 금액 조건 충족 여부 |
| `appliedYn` | `Y` / `N` | 해당 상품에 쿠폰 자동 적용 여부 |
| `remainDay` | `4` | 쿠폰 만료까지 남은 일수 |
| `tagList` | `["뷰티","설날"]` | 프로모션 태그 (UI 필터링·표시용) |

## 결제 준비/진행 플로우 (프로젝트 기준)

### Step 1. 주문번호 발급 — `POST /api/v1/orders/order-no`
- **Request**: `{ isNewOrderForm: true }`
- **Response 최소 필드**:
  - `orderNo`, `orderSignature`, `timestamp`, `orderVerifyKey`, `orderKey`

### Step 2. 결제 Ready 등록 — `POST /api/v1/orders/{orderNo}/ready`
- **목적**: 전체 주문 정보(상품, 금액, 배송지, 할인, 결제수단)를 서버에 등록하고 결제 직전 상태 확정
- **무신사 실제 Payload 구조** (카테고리별 정리):
  | 카테고리 | 필드 | 설명 |
  |----------|------|------|
  | 주문 식별 | `orderNo`, `orderSignature`, `timestamp`, `ord_key`, `ord_verify_key` | 주문번호 발급 응답값 그대로 전달 |
  | 상품 | `good_name`, `good_mny`, `master_goods_no`, `cartIdsText` | 대표 상품명, 결제금액, 상품번호, 장바구니 ID |
  | 금액 | `orderAmount { normalAmount, saleAmount, payAmount, deliveryAmount }` | 정가 → 판매가 → 결제액 → 배송비 |
  | 할인 | `discount { usePointAmount, couponAmount, cartCouponAmount, memberAndAdDiscountAmount }` | 포인트, 쿠폰, 장바구니쿠폰, 회원할인 |
  | 수령인 | `recipient { addressId, title, name, mobile, zipcode, address1, address2 }` | 배송지 전체 정보 |
  | 배송 메모 | `dlv_msg` | 공동현관 비밀번호 등 포함 |
  | 결제수단 | `pay_kind` (`KAKAOPAY`), `pgKind` (`kkopay`) | 결제수단 + PG사 |
  | 현금영수증 | `cashReceipt { registrationType, numberType, registrationNumber }` | 소득공제/지출증빙, 휴대폰/사업자번호 |
  | 세금 | `comm_tax_mny`, `comm_vat_mny`, `comm_free_mny` | 과세/부가세/면세 금액 |
  | PG 콜백 | `Ret_URL` | 결제 완료 후 PG가 호출할 서버 URL |
- **금액 검증 예시**: `149,000(정가) - 10,320(포인트) - 1,490(회원할인) = 137,190(결제액)`
- **레거시 호환 필드**: `ord_no`/`orderNo`, `rcvr_name`/`rcvr_nm` 등 동일 데이터가 구/신 키로 중복 전달 (API 버전 진화 흔적)
- **Request 최소 필드** (우리 프로젝트):
  - `orderNo`, `orderSignature`, `timestamp`, `orderKey`, `orderVerifyKey`
  - `paymentMethod`, `pgKind`
  - `orderAmount { normalAmount, saleAmount, payAmount, deliveryAmount }`
  - `discount { usePointAmount, couponAmount, cartCouponAmount }`
  - `recipient { name, mobile, zipcode, address1, address2 }`, `dlvMsg`
  - `cartItemIds[]`
- **서버 처리**:
  1. 주문번호 서명 검증
  2. 주문 조회/생성, 금액 정합성 검증
  3. 결제(Payment) PENDING 상태로 생성
  4. `orderNo + orderKey + READY` 멱등 처리
- **Response**: `{ mpToken: null }` — 무신사페이 미사용 시 null

### Step 3. 결제 세션 생성 — `POST /api/v1/orders/payment-session`
- **목적**: PG사에 결제를 요청하고 사용자를 리다이렉트할 결제 URL 확보
- **역할 분리**: ready = 주문 전체 정보 등록, payment-session = PG 결제에 필요한 최소 정보만
- **무신사 실제 Payload**:
  | 필드 | 예시 | 설명 |
  |------|------|------|
  | `orderNo` | `202602211010540001` | 주문번호 |
  | `payKind` | `KAKAOPAY` | 결제수단 |
  | `payAmount` | `137190` | 결제 금액 |
  | `member` | `{ name, email, phone }` | 결제자 정보 (PG 전달용) |
  | `goods[]` | `[{ goodsNo, goodsOptionNo, extraOptionIds }]` | 상품 목록 |
  | `cashReceipt` | `{ registrationType, numberType, registrationNumber }` | 현금영수증 (ready와 동일값 재전달) |
  | `taxFreeAmount` | `0` | 면세 금액 |
  | `cardQuota` | `null` | 할부 개월 (카드 결제 시) |
  | `mpToken` | `""` | 무신사페이 토큰 (ready 응답값) |
- **참고**: `orderKey`는 이 단계에서 전달하지 않음 (ready 단계에서만 사용)
- **Request 최소 필드** (우리 프로젝트):
  - `orderNo`, `paymentMethod`, `payAmount`
  - `member { name, email, phone }`
  - `goods[]` (`productId`, `optionId`, `quantity`)
  - `taxFreeAmount`, `cardQuota` (선택)
- **서버 처리**:
  1. `orderNo` 기준 주문 조회
  2. PG사에 결제 요청 → `paymentKey`, `paymentUrl` 수신
  3. `orderNo + orderKey + PAYMENT_SESSION` 멱등 처리
- **Response**:
  | 필드 | 예시 | 설명 |
  |------|------|------|
  | `orderNo` | `202602211010540001` | 주문번호 |
  | `paymentKey` | `PP5STE6221KAK001...` | PG 결제 고유 키 (PG 식별자 포함: `KAK`=카카오, `TOSVI`=토스) |
  | `amount` | `137190` | 결제 금액 |
  | `paymentUrl` | `https://pay.musinsapayments.com/mps/payment/...` | PG 결제 페이지 URL |
  | `pgKind` | `KAKAO` | PG사 식별 (`payKind`와 값이 다를 수 있음) |
  | `virtualAccount` | `null` | 가상계좌 정보 (해당 시에만) |

### Step 4. 주문 최종 처리 — `POST /api/v1/orders/process`

PG 결제 완료 후 **서버 채널**과 **브라우저 채널** 두 경로가 동시 발생:

- **서버 채널** (PG → 서버, 상태 정합성의 보조 확인):
  1. PG사가 `Ret_URL`(`/api/v1/orders/payment-check`)로 결제 결과 전달
  2. Payment 상태 확인/갱신

- **브라우저 채널** (PG → 브라우저 → 서버, 주문 최종 확정):
  1. PG 결제 완료 → PG가 브라우저를 `/order-process`로 리다이렉트 (PG 결과값 포함)
  2. `/order-process` 페이지가 자동으로 `POST /orders/process` 전송
  3. 서버가 주문 최종 확정 → 302 리다이렉트 → `/order_result/{orderNo}?code=0`

- **`POST /orders/process` 상세**:
  - **Content-Type**: `application/x-www-form-urlencoded` (Form Data, JSON 아님)
  - **Form Data인 이유**: PG사가 브라우저 리다이렉트 시 결제 결과를 Form으로 전달하고, 그 데이터를 원본 주문 데이터와 합쳐서 서버에 재전송하는 구조
  - **Payload 구성**: ready 단계의 주문 데이터 + PG 결제 결과 데이터가 합쳐짐
  - **PG 결제 결과 필드** (이 단계에서 새로 추가되는 값):
    | 필드 | 예시 | 설명 |
    |------|------|------|
    | `res_cd` | `0000` | PG 결제 결과 코드 (`0000` = 성공) |
    | `res_msg` | `정상처리` | PG 결제 결과 메시지 |
    | `payment_key` | `PP5STE6221KAK004...` | PG 결제 키 (payment-session 응답값) |
    | `auth_token` | JWT 토큰 | PG 인증 토큰 |
    | `enc_code` | `68549268811c...` | PG 암호화 코드 (결제 검증용) |
    | `amount` | `11050` | PG가 확인한 결제 금액 |
  - **서버 처리 추정**:
    1. `res_cd == "0000"` 확인 (PG 결제 성공 여부)
    2. `amount`와 `order_pay_amt` 일치 검증 (금액 위변조 방지)
    3. `auth_token`, `enc_code`로 PG 결제 진위 검증
    4. Order 상태 → `PAID`, Payment 상태 → `PAID`
    5. 302 리다이렉트 → `/order_result/{orderNo}?code=0`
  - **Response**: 302 Redirect (브라우저가 즉시 이동하여 응답 body 캡처 불가)
  - `code=0`: 결제 성공 / 그 외: 실패·취소

## 무신사 레퍼런스와의 관계
- 무신사는 `order-form`, `plcc`, `order-no`, `payment-session`, `ready` 단계에서 매우 많은 필드를 사용한다.
- 우리 프로젝트는 아래 원칙으로 단순화한다.
  - 결제 URL 확보, 주문 식별/검증, 금액/상품 정합성에 필요한 필드만 유지
  - 화면 표시/통계/제휴 목적의 부가 필드는 필요 시 별도 API로 분리

## PLCC 조회 (`GET /api2/order/v1/plcc?channelCode=ORDER`) 반영 원칙
- PLCC는 결제수단 노출/프로모션 안내를 위한 **부가 정보**로 취급
- 주문 생성/결제 핵심 플로우의 필수 선행 조건으로 두지 않음
- 필요 시 프론트 표시용 API로 별도 도입하되, 주문 트랜잭션과 분리

## 주문 상태 (OrderStatus)

| 상태 | 설명 | 전이 조건 |
|------|------|----------|
| `INIT` | 주문 생성 직후 | 주문 생성 시 |
| `PENDING` | 결제 시도 중 | 결제 세션/ready 이후 |
| `PAID` | 결제 완료 | 결제 성공 이벤트 수신 시 |
| `FAILED` | 주문 실패 | 결제 실패 이벤트 수신 시 |
| `CANCELED` | 취소 (환불 포함) | 클레임 도메인 취소+환불 실행 시 (상세: [order-cancel.md](order-cancel.md)) |
| `COMPLETED` | 구매 확정 | 배송 완료 후 자동/수동 확정 |

```text
INIT -> PENDING -> PAID -> COMPLETED
                 \-> FAILED
            CANCELED
```
