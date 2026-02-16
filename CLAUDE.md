# CLAUDE.md - gamsung-commerce (Backend API)

## 프로젝트 개요
무신사 스타일 이커머스 백엔드 API. Spring Boot + Gradle 멀티모듈 구조.

## 기술 스택
- Java 17+, Spring Boot, Gradle
- MySQL, Redis (캐시), Kafka
- JPA/Hibernate, Spring Data

## 프로젝트 구조
```
apps/commerce-api/src/main/java/com/loopers/
├── domain/          # 도메인 레이어 (Entity, Service, Repository 인터페이스, Info, Command)
├── infrastructure/  # 인프라 레이어 (JPA Repository 구현체, Cache 구현체)
├── interfaces/api/  # API 레이어 (Controller, Dto, ApiSpec)
└── support/         # 공통 지원 (ErrorType, BaseEntity 등)
```

## 아키텍처 패턴
- **레이어드 아키텍처**: interfaces → domain → infrastructure
- **도메인 서비스**: `XxxService`는 domain 패키지에 위치
- **Repository 패턴**: domain에 인터페이스, infrastructure에 구현체
- **캐시 패턴**: Redis 캐시 우선 조회 후 DB fallback (`BrandCacheRepository` 참고)
- **API 응답 래퍼**: `ApiResponse<T>` (meta + data 구조)
- **API 스펙 인터페이스**: Swagger 문서화용 `XxxV1ApiSpec` 인터페이스를 Controller가 구현
- **DTO 패턴**: `XxxV1Dto` 내부에 record로 Request/Response 정의
- **도메인 정보 객체**: `XxxInfo` record로 도메인 데이터 전달
- **커맨드 객체**: `XxxCommand` 내부에 요청 데이터 정의

## 연관 프로젝트
- 프론트엔드: `/Users/cony/Desktop/workspace/gamsung-web` (Next.js)

## Git 커밋 메시지 규칙

### 형식
`<type>(<scope>): <subject>`

### 언어
- **subject와 본문은 반드시 한글**로 작성. type과 scope만 영문 소문자.
- subject: 한 줄, 50자 내외, 명령형, 마침표 없음

### Type
feat(기능), fix(버그수정), docs(문서), style(포맷), refactor(리팩터링), test(테스트), chore(빌드/설정)

### Scope
변경된 모듈/도메인: brand, product, order, payment, likes 등

### 예시
```
feat(product): 상품 목록 좋아요 정렬 추가
fix(payment): PG 타임아웃 시 재시도 로직 수정
refactor(likes): LikeProductFacade로 좋아요 조회 통합
```

한 커밋에 여러 도메인이 있으면 가장 비중 큰 변경 기준으로 scope 하나만 사용.
