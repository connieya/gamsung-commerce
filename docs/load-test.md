# 부하 테스트 진행 방법

상품 목록 API 등에 대한 부하 테스트는 **k6**로 수행하며, **loopers_qa** DB에 대량 데이터(Zipf 분포)를 넣어 비정규화 전/후 성능 차이를 비교할 수 있습니다.

---

## 1. 개요

| 항목 | 설명 |
|------|------|
| 도구 | [k6](https://k6.io/) (HTTP 부하 테스트) |
| 대상 DB | **loopers_qa** (로컬 웹용 **loopers**와 분리) |
| 데이터 | 상품 50만, 좋아요 500만(멱법칙), 유저 5만 등 — `qa/sample/scripts/generate-all-data.js` |
| API 프로필 | **qa** (DataLocalLoader 미실행, loopers_qa 연결) |

---

## 2. 사전 요구사항

- **Docker** (MySQL·Redis·Kafka 등 인프라)
- **Node.js** (데이터 생성 스크립트 실행)
- **k6** 설치  
  - macOS: `brew install k6`  
  - [공식 설치 가이드](https://k6.io/docs/get-started/installation/)

---

## 3. 진행 순서

### 3.1 인프라 기동

```bash
# 프로젝트 루트(gamsung-commerce)에서
docker-compose -f ./docker/infra-compose.yml up -d
docker ps  # MySQL 등 정상 기동 확인
```

### 3.2 loopers_qa DB 및 스키마 준비 (최초 1회)

`loopers_qa` DB가 없거나 테이블이 비어 있으면, `loopers` 스키마를 복사합니다.

```bash
# 프로젝트 루트에서 (root 비밀번호 기본값: root)
MYSQL_ROOT_PASSWORD=root ./qa/init-schema-loopers-qa.sh
```

- Docker를 막 올린 경우: 위 스크립트 한 번 실행으로 DB 생성 + 스키마 복사
- 이미 **loopers**를 쓰고 있는 경우: 동일하게 한 번 실행

> 상세: [docs/docker.md § 4.6 부하 테스트용 DB (loopers_qa)](docker.md#46-부하-테스트용-db-loopers_qa)

### 3.3 대량 데이터 삽입

`insert-all-data.sh`가 **generate-all-data.js**로 SQL을 생성한 뒤 **loopers_qa**에 넣습니다. (기본 `DB_NAME=loopers_qa`)

```bash
cd qa/sample
./insert-all-data.sh
```

- 생성 규모: brand 414, users 5만, product 50만, product_like 500만(Zipf), like_summary 50만 행
- SQL 생성·삽입에 수 분 걸릴 수 있습니다.

### 3.4 commerce-api 기동 (qa 프로필)

부하 테스트 시 API는 **loopers_qa**를 바라보도록 **qa** 프로필로 실행합니다.

```bash
# 프로젝트 루트에서
SPRING_PROFILES_ACTIVE=qa ./gradlew :apps:commerce-api:bootRun
```

- 기동이 끝나면 `http://localhost:8080` 에서 API가 동작합니다.

### 3.5 k6 실행

다른 터미널에서 k6 스크립트를 실행합니다.

```bash
cd qa/k6/scripts

# 기본: 상품 목록 API (일반 엔드포인트)
k6 run product-list.js

# 최적화 엔드포인트용
k6 run product-list-optimized.js
```

- 정렬 타입을 바꾸려면 환경 변수 `SORT_TYPE` 사용 (예: `LATEST_DESC`, `PRICE_ASC`, `LIKE_DESC` 등)
- VU·시간은 스크립트 내 `options` 또는 CLI로 오버라이드 가능합니다.

**예시**

```bash
# 가격 오름차순으로 1분
SORT_TYPE=PRICE_ASC k6 run product-list.js

# VU 50, 60초 (스크립트 options 대신 CLI)
k6 run -u 50 -d 60s product-list.js
```

---

## 4. 결과 확인

- k6 실행이 끝나면 터미널에 요약이 출력됩니다 (체크 통과율, `http_req_duration`, `iterations` 등).
- 과거 측정·쿼리 분석은 `qa/k6/report/*.md` 에 정리되어 있습니다.

---

## 5. 관련 파일

| 경로 | 설명 |
|------|------|
| `qa/sample/insert-all-data.sh` | 대량 데이터 SQL 생성 + loopers_qa 삽입 |
| `qa/sample/scripts/generate-all-data.js` | brand/users/product/product_like/like_summary 생성 (Zipf) |
| `qa/k6/scripts/product-list.js` | 상품 목록 API 부하 (일반) |
| `qa/k6/scripts/product-list-optimized.js` | 상품 목록 API 부하 (최적화 엔드포인트) |
| `qa/k6/report/*.md` | 과거 부하 테스트·쿼리 분석 리포트 |
| `qa/init-schema-loopers-qa.sh` | loopers_qa DB 생성 및 스키마 복사 |
| `docs/docker.md` | Docker·MySQL·loopers_qa 요약 |
