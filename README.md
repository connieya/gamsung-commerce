# E-Commerce (Spring + Java)

이커머스 도메인의 멀티 모듈 스프링 프로젝트입니다.

## Getting Started

### Environment

`local` 프로필로 동작할 수 있도록, 필요 인프라를 `docker-compose`로 제공합니다.

```shell
docker-compose -f ./docker/infra-compose.yml up -d
```

> **Docker 사용법 상세** (기동·종료·MySQL 접속, 다른 PC 실행 시 참고): [docs/docker.md](docs/docker.md)

### Monitoring

`local` 환경에서 모니터링을 할 수 있도록, `docker-compose`로 Prometheus와 Grafana를 제공합니다.  
애플리케이션 실행 후 **http://localhost:3000**에서 admin/admin 계정으로 로그인해 확인할 수 있습니다.

```shell
docker-compose -f ./docker/monitoring-compose.yml up
```

## Development

- **빌드**: `./gradlew build`
- **실행**: 각 앱별로 `./gradlew :apps:<app-name>:bootRun`
  - commerce-api (포트 8080): `./gradlew :apps:commerce-api:bootRun`
  - order-api (포트 8081): `./gradlew :apps:order-api:bootRun`
  - like-api (포트 8083): `./gradlew :apps:like-api:bootRun`
- **API 테스트**: `http/` 디렉터리의 `.http` 파일과 `http-client.env.json`을 사용합니다.

### 시드 데이터 (local)

`commerce-api`를 **local** 프로필로 실행하면 **서버 기동 시 자동으로** 무신사 스타일 시드 데이터(브랜드, 유저, 상품, 좋아요)가 DB에 삽입됩니다. 별도 수동 작업 없이 API/웹에서 바로 목록을 확인할 수 있습니다.

- 시드 스크립트: `apps/commerce-api/src/main/resources/data-local.sql`
- local 프로필에서는 `ddl-auto: create`로 테이블이 매 기동 시 재생성된 뒤, 위 SQL이 실행됩니다.

### 부하 테스트 (k6)

상품 목록 API 등에 대한 부하 테스트는 **loopers_qa** DB에 대량 데이터를 넣은 뒤 **k6**로 실행합니다. 진행 순서·사전 요구사항·관련 파일은 아래 문서를 참고하세요.

> **[docs/load-test.md](docs/load-test.md)** — 부하 테스트 진행 방법

### 트러블슈팅

- **[E2E 테스트 개별 실행 실패 및 test 프로필·Redis·DDL 설정 정리](https://github.com/connieya/gamsung-commerce/issues/30)** — 테스트 격리성, Redis NPE, Table doesn't exist 등

## Architecture

앱 모듈(commerce-api, order-api, commerce-collector 등)은 **클린 아키텍처**를 따릅니다.

- **interfaces** → HTTP·메시지 진입 (컨트롤러, DTO)
- **application** → 유스케이스 조합 (Facade, UseCase)
- **domain** → 비즈니스 로직, Repository 인터페이스
- **infrastructure** → JPA·캐시·외부 API 구현체
- **support** → 공통 예외·에러 처리

의존성은 domain ← application ← interfaces, domain ← infrastructure 방향만 허용합니다.
자세한 패키지·네이밍 규칙은 `.cursor/rules/clean-architecture-conventions.mdc`를 참고하세요.

### 서비스 간 통신

각 서비스는 **REST(Feign)** 으로 통신합니다. 서비스별 Internal API(`/internal/v1/*`)를 통해 데이터를 주고받습니다.

| 방향 | 호출 목적 |
|------|----------|
| order-api → commerce-api | 사용자 조회, 상품 목록 조회, 쿠폰 할인 계산, 결제 준비 |
| commerce-api → order-api | 주문 조회(ID/번호), 주문 완료 처리 |
| like-api → commerce-api | 사용자 검증, 상품 정보 벌크 조회 |

좋아요 데이터는 **CQRS** 패턴으로 관리합니다. like-api가 원본 데이터(Like, LikeSummary)를 소유하고, Kafka(`like-update-topic-v1`)를 통해 commerce-collector가 commerce-api DB의 LikeSummary 읽기 모델을 동기화합니다.

## Documentation

- **이커머스 도메인 기술 명세 규칙**: [`.cursor/rules/ecommerce-domain-knowledge.mdc`](.cursor/rules/ecommerce-domain-knowledge.mdc) — 도메인 지식을 어떻게 정리할지에 대한 규칙과 템플릿

## Domain Specification

도메인별 비즈니스 로직 및 상세 설계 명세입니다.

- [주문 (Order)](docs/domain-spec/order.md) : 주문번호 설계, 주문 플로우
- [결제 (Payment)](docs/domain-spec/payment.md) : 결제 승인 플로우, 멱등성 보장
- [장바구니 (Cart)](docs/domain-spec/cart.md) : 장바구니 도메인 모델, API 스펙
- [주문 취소 (Order Cancel)](docs/domain-spec/order-cancel.md) : 주문 취소 플로우 (무신사 레퍼런스)

## Multi-Module 구조

멀티 모듈로 위계와 역할을 나누어 적용합니다.

- **apps**: 실행 가능한 Spring Boot 애플리케이션
- **modules**: 도메인/구현에 무관한 재사용 가능한 설정 모듈
- **supports**: 로깅·모니터링·직렬화 등 부가 기능

```
Root
├── apps ( Spring Boot 애플리케이션 )
│   ├── commerce-api      # 상품, 결제, 쿠폰, 랭킹 등 (포트 8080)
│   ├── order-api         # 주문, 장바구니 (포트 8081)
│   ├── like-api          # 좋아요 (포트 8083)
│   ├── commerce-collector # 이벤트 수집·메트릭
│   ├── commerce-batch    # 배치 작업
│   └── pg-simulator      # PG 결제 시뮬레이터 (Kotlin, 포트 8082)
├── modules ( 재사용 설정 )
│   ├── feign   # Feign + Resilience4j
│   ├── jpa     # JPA, QueryDSL, DataSource
│   ├── kafka   # Kafka 설정
│   └── redis   # Redis 캐시/세션
└── supports ( 부가 기능 )
    ├── jackson   # JSON 직렬화 설정
    ├── logging   # Logback (JSON/Plain, Slack), AOP 레이어 로깅, 런타임 로그 모드 전환
    └── monitoring # Actuator, Prometheus
```
