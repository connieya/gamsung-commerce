# 상품 목록 API 성능 최적화 리포트

## 목적

상품 목록 조회 API에 대해 **인덱스, 쿼리 최적화, 비정규화, 캐싱**을 단계별로 적용하고,
각 단계의 부하 테스트 결과를 비교하여 백엔드 성능 최적화 역량을 학습한다.

---

## 테스트 환경

| 항목 | 값 |
|------|-----|
| DB | MySQL 8 (Docker) |
| 상품 수 | 500,000건 |
| 좋아요 수 | 5,000,000건 |
| Spring Profile | qa (`loopers_qa` DB) |
| k6 VU | 10 |
| k6 Duration | 60s |
| 정렬 기준 | 좋아요 내림차순 |

---

## 최적화 단계별 결과

### 1단계: 원본 쿼리 (LEFT JOIN + GROUP BY + COUNT)

**엔드포인트:** `GET /api/v1/products?productSort=LIKES_DESC`
**스크립트:** `product-list.js`

**쿼리:**
```sql
SELECT p.id, p.price, p.name, b.name, p.image_url,
       CAST(COUNT(pl.id) AS SIGNED) AS likeCount,
       p.released_at
FROM product p
LEFT JOIN brand b ON p.ref_brand_id = b.id
LEFT JOIN product_like pl ON p.id = pl.ref_product_id
GROUP BY p.id, p.price, p.name, b.name, p.image_url, p.released_at
ORDER BY likeCount DESC
LIMIT 5
```

**실행 계획 (EXPLAIN):**
```
+----+-------------+-------+--------+---------------+-----------------------------+---------+---------------------------+---------+----------+---------------------------------------------------------+
| id | select_type | table | type   | possible_keys | key                         | key_len | ref                       | rows    | filtered | Extra                                                   |
+----+-------------+-------+--------+---------------+-----------------------------+---------+---------------------------+---------+----------+---------------------------------------------------------+
|  1 | SIMPLE      | p     | ALL    | NULL          | NULL                        | NULL    | NULL                      |  496254 |   100.00 | Using temporary; Using filesort                         |
|  1 | SIMPLE      | b     | eq_ref | PRIMARY       | PRIMARY                     | 8       | loopers_qa.p.ref_brand_id |       1 |   100.00 | NULL                                                    |
|  1 | SIMPLE      | pl    | index  | NULL          | UKi88ydvxuyj9djsf1yaq18mdpb | 16      | NULL                      | 4859458 |   100.00 | Using where; Using index; Using join buffer (hash join) |
+----+-------------+-------+--------+---------------+-----------------------------+---------+---------------------------+---------+----------+---------------------------------------------------------+
```

**병목 분석:**
- `p` (product): **ALL (풀 테이블 스캔)** — 50만 행 전체를 읽음, 인덱스 미사용
- `pl` (product_like): **index (인덱스 풀 스캔)** — 500만 행 전체를 스캔하여 hash join
- `Using temporary` — GROUP BY 처리를 위해 임시 테이블 생성
- `Using filesort` — ORDER BY를 위해 전체 결과를 다시 정렬
- 5건을 가져오기 위해 **550만 행을 읽고 집계** → 단건 응답에 26초 이상 소요

**k6 결과:**

| 지표 | 값 |
|------|-----|
| http_req_duration (avg) | 59.99s (타임아웃) |
| http_req_duration (p95) | 59.99s |
| http_req_failed | 100% (10/10) |
| http_reqs (TPS) | 0.17/s |
| iterations | 10 |

> 1분 동안 VU 10명이 각각 1번씩만 요청, 전부 타임아웃 실패


---

### 2단계: 서브쿼리 최적화

**엔드포인트:** `GET /api/v1/products/optimized?productSort=LIKES_DESC`
**스크립트:** `product-list-optimized.js`

**쿼리:**
```sql
SELECT p.id, p.price, p.name, b.name, p.image_url,
       CAST((SELECT COUNT(l) FROM product_like l WHERE l.ref_product_id = p.id) AS SIGNED) AS likeCount,
       p.released_at
FROM product p
LEFT JOIN brand b ON p.ref_brand_id = b.id
ORDER BY likeCount DESC
LIMIT 5
```

**변경 내용:**
- LEFT JOIN + GROUP BY 대신 스칼라 서브쿼리로 좋아요 수를 조회
- GROUP BY 제거로 집계 비용 감소

**실행 계획 (EXPLAIN):**
```
+----+--------------------+-------+--------+---------------+-----------------------------+---------+---------------------------+---------+----------+---------------------------------+
| id | select_type        | table | type   | possible_keys | key                         | key_len | ref                       | rows    | filtered | Extra                           |
+----+--------------------+-------+--------+---------------+-----------------------------+---------+---------------------------+---------+----------+---------------------------------+
|  1 | PRIMARY            | p     | ALL    | NULL          | NULL                        | NULL    | NULL                      |  496254 |   100.00 | Using temporary; Using filesort |
|  1 | PRIMARY            | b     | eq_ref | PRIMARY       | PRIMARY                     | 8       | loopers_qa.p.ref_brand_id |       1 |   100.00 | NULL                            |
|  2 | DEPENDENT SUBQUERY | l     | index  | NULL          | UKi88ydvxuyj9djsf1yaq18mdpb | 16      | NULL                      | 4859458 |    10.00 | Using where; Using index        |
+----+--------------------+-------+--------+---------------+-----------------------------+---------+---------------------------+---------+----------+---------------------------------+
```

**병목 분석:**
- `p` (product): **ALL (풀 테이블 스캔)** — 1단계와 동일하게 50만 행 전체 읽음
- `l` (product_like): **DEPENDENT SUBQUERY** — product의 **각 행마다** 서브쿼리 실행
  - 50만 행 × 500만 행 인덱스 풀스캔 = 이론상 최악의 경우 2.5조 행 접근
  - `filtered: 10%`로 실제로는 줄어들지만 여전히 막대한 비용
  - `ref_product_id`에 인덱스가 없어서 매번 인덱스 풀스캔 발생
- GROUP BY는 제거되었지만 `Using temporary; Using filesort`는 여전히 발생
- 1단계의 1회 hash join보다 DEPENDENT SUBQUERY가 오히려 더 비효율적일 수 있음

**k6 결과:**

| 지표 | 값 |
|------|-----|
| http_req_duration (avg) | 59.99s (타임아웃) |
| http_req_duration (p95) | 59.99s |
| http_req_failed | 100% (10/10) |
| http_reqs (TPS) | 0.17/s |
| iterations | 10 |

> 1단계와 동일하게 전부 타임아웃. 스칼라 서브쿼리도 50만 행마다 COUNT 서브쿼리 실행 → 인덱스 없이는 개선 효과 미미

---

### 3단계: 비정규화 (LikeSummary 테이블)

**엔드포인트:** `GET /api/v1/products/denormalized/no-brand?productSort=DENORMALIZED_LIKES_DESC`
**스크립트:** `product-list-denormalized.js`

**쿼리:**
```sql
SELECT p.id, p.price, p.name, b.name, p.image_url,
       s.like_count,
       p.released_at
FROM like_summary s
JOIN product p ON s.ref_target_id = p.id
LEFT JOIN brand b ON p.ref_brand_id = b.id
WHERE s.target_type = 'PRODUCT'
ORDER BY s.like_count DESC
LIMIT 5
```

**변경 내용:**
- 좋아요 수를 `like_summary` 테이블에 미리 집계하여 저장 (비정규화)
- COUNT 집계 없이 단순 JOIN으로 조회 → 500만 행 스캔 불필요

**실행 계획 (EXPLAIN):**
```
+----+-------------+-------+--------+-----------------------------+---------+---------+---------------------------+--------+----------+-----------------------------+
| id | select_type | table | type   | possible_keys               | key     | key_len | ref                       | rows   | filtered | Extra                       |
+----+-------------+-------+--------+-----------------------------+---------+---------+---------------------------+--------+----------+-----------------------------+
|  1 | SIMPLE      | s     | ALL    | UKaqw1do2xdd90a3o0aneikiq8y | NULL    | NULL    | NULL                      | 498250 |    50.00 | Using where; Using filesort |
|  1 | SIMPLE      | p     | eq_ref | PRIMARY                     | PRIMARY | 8       | loopers_qa.s.target_id    |      1 |   100.00 | NULL                        |
|  1 | SIMPLE      | b     | eq_ref | PRIMARY                     | PRIMARY | 8       | loopers_qa.p.ref_brand_id |      1 |   100.00 | NULL                        |
+----+-------------+-------+--------+-----------------------------+---------+---------+---------------------------+--------+----------+-----------------------------+
```

**병목 분석:**
- `s` (like_summary): **ALL (풀 테이블 스캔)** — 50만 행 전체 읽음, 인덱스 미사용
  - `filtered: 50%` — `WHERE target_type = 'PRODUCT'` 조건으로 절반 필터링
  - `Using filesort` — `like_count` 정렬을 위해 50만 행 정렬
- `p`, `b`: **eq_ref (PK 조인)** — 문제 없음
- 1~2단계 대비 개선: product_like 500만 행 스캔 제거, GROUP BY + 임시 테이블 제거
- 남은 병목: like_summary 풀스캔 + filesort → **인덱스로 추가 개선 가능**

**k6 결과:**

| 지표 | 값 |
|------|-----|
| http_req_duration (avg) | 1.85s |
| http_req_duration (p95) | 3.83s |
| http_req_failed | 0% (0/311) |
| http_reqs (TPS) | 5.09/s |
| iterations | 311 |

> 1단계 대비 avg 59.99s → 1.85s, TPS 0.17/s → 5.09/s, 실패율 100% → 0%
> 아직 like_summary 풀 테이블 스캔 + filesort 발생 → 인덱스로 추가 개선 가능

---

### 4단계: 인덱스 적용 (like_summary)

**엔드포인트:** `GET /api/v1/products/denormalized/no-brand?productSort=DENORMALIZED_LIKES_DESC` (3단계와 동일)
**스크립트:** `product-list-denormalized.js`

**적용 인덱스:**
```sql
CREATE INDEX idx_like_summary_type_count ON like_summary (target_type, like_count);
```

**실행 계획 (EXPLAIN):**
```
+----+-------------+-------+--------+-------------------------------------------------------+-----------------------------+---------+---------------------------+--------+----------+------------------------------------+
| id | select_type | table | type   | possible_keys                                         | key                         | key_len | ref                       | rows   | filtered | Extra                              |
+----+-------------+-------+--------+-------------------------------------------------------+-----------------------------+---------+---------------------------+--------+----------+------------------------------------+
|  1 | SIMPLE      | s     | ref    | UKaqw1do2xdd90a3o0aneikiq8y,idx_like_summary_type_count | idx_like_summary_type_count | 2       | const                     | 249125 |   100.00 | Using where; Backward index scan   |
|  1 | SIMPLE      | p     | eq_ref | PRIMARY                                               | PRIMARY                     | 8       | loopers_qa.s.target_id    |      1 |   100.00 | NULL                               |
|  1 | SIMPLE      | b     | eq_ref | PRIMARY                                               | PRIMARY                     | 8       | loopers_qa.p.ref_brand_id |      1 |   100.00 | NULL                               |
+----+-------------+-------+--------+-------------------------------------------------------+-----------------------------+---------+---------------------------+--------+----------+------------------------------------+
```

**데이터 조회 실행 계획 분석 (3단계 대비):**
- `s` (like_summary): **ALL → ref** (풀스캔 → 인덱스 사용)
- **Using filesort 제거** → `Backward index scan`으로 정렬 없이 인덱스 역순 탐색
- `target_type = 'PRODUCT'` 조건으로 인덱스를 타고, `like_count` 내림차순으로 바로 상위 5건 조회

**k6 결과:**

| 지표 | 값 |
|------|-----|
| http_req_duration (avg) | 14.85s |
| http_req_duration (p95) | 20.57s |
| http_req_failed | 0% (0/48) |
| http_reqs (TPS) | 0.65/s |
| iterations | 48 |

> 3단계(avg 1.85s) 대비 오히려 **8배 느려짐** — EXPLAIN상 데이터 조회는 인덱스를 타지만 전체 응답은 악화

**원인 분석: COUNT 쿼리가 병목**

Spring Data JPA의 `Page<>` 반환 시 페이징을 위한 COUNT 쿼리가 자동 실행된다:
```sql
-- 데이터 조회 (인덱스 적용) → 0.015초 ✅
SELECT ... FROM like_summary s JOIN product p ... WHERE s.target_type = 'PRODUCT'
ORDER BY s.like_count DESC LIMIT 5

-- COUNT 쿼리 (매 요청마다 실행) → 8.95초 ❌
SELECT COUNT(*) FROM like_summary s JOIN product p ... WHERE s.target_type = 'PRODUCT'
```

| | 인덱스 없음 (3단계) | 인덱스 있음 (4단계) |
|---|---|---|
| 데이터 조회 | 풀스캔 + filesort | **0.015초** (인덱스 + backward scan) |
| COUNT 쿼리 | **3.09초** | **8.95초** (3배 느려짐) |
| COUNT 실행 계획 | `index` (UK 커버링 인덱스 스캔, `Using index`) | `ref` (새 인덱스 사용, `Using index condition`) |

**인덱스가 COUNT를 느리게 만든 이유:**
- **인덱스 없을 때**: 기존 UK `(target_id, target_type)`로 **커버링 인덱스 스캔** — 테이블 접근 없이 인덱스만으로 COUNT 처리
- **인덱스 있을 때**: MySQL이 새 인덱스 `(target_type, like_count)`를 선택하지만 `Using index condition` (ICP) — 인덱스에서 1차 필터 후 **테이블로 돌아가서 추가 확인**, 이 과정에서 랜덤 I/O 발생

**교훈:**
- 인덱스 추가가 항상 성능을 개선하는 것은 아니다
- MySQL 옵티마이저가 새 인덱스를 선택하면서 다른 쿼리(COUNT)의 실행 계획이 바뀔 수 있다
- `Page<>` 사용 시 데이터 조회 쿼리뿐 아니라 **COUNT 쿼리의 성능도 함께 고려**해야 한다

---

### 5단계: 캐싱 적용 (Redis)

> TODO: 인덱스 적용 후, Redis 캐싱을 통한 DB 부하 감소 효과 측정

---

## 단계별 비교 요약

| 단계 | 전략 | avg | p95 | TPS | 실패율 | 비고 |
|------|------|-----|-----|-----|--------|------|
| 1 | JOIN + GROUP BY + COUNT | 59.99s | 59.99s | 0.17/s | 100% | 전부 타임아웃 |
| 2 | 스칼라 서브쿼리 | 59.99s | 59.99s | 0.17/s | 100% | 1단계와 동일 (타임아웃) |
| 3 | 비정규화 (LikeSummary) | 1.85s | 3.83s | 5.09/s | 0% | 1단계 대비 TPS 30배 향상 |
| 4 | 인덱스 (like_summary) | 14.85s | 20.57s | 0.65/s | 0% | 인덱스가 COUNT 쿼리를 3배 느리게 만듦 |
| 5 | 캐싱 (Redis) | - | - | - | - | |

---

## 학습 포인트

- [ ] JOIN + GROUP BY + COUNT 가 대량 데이터에서 왜 느린지 이해
- [ ] 스칼라 서브쿼리와 JOIN 집계의 실행 계획 차이
- [ ] 비정규화의 트레이드오프 (쓰기 비용 증가 vs 읽기 성능 향상)
- [ ] 인덱스가 쿼리 실행 계획에 미치는 영향 (EXPLAIN ANALYZE)
- [ ] 캐싱 전략 (Cache Aside, TTL, 무효화)
- [ ] TPS, p95, avg 등 성능 지표의 의미와 활용
